package com.mentorai.progress.service;

import com.mentorai.auth.service.AuthService;
import com.mentorai.common.exception.ConflictException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.progress.dto.*;
import com.mentorai.progress.entity.*;
import com.mentorai.progress.entity.ProgressEnums.Outcome;
import com.mentorai.progress.repository.*;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.dto.UpdateRoadmapRequest;
import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.roadmap.service.RoadmapService;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyProgressService {
    private static final int HISTORY_SIZE = 20;

    private final WeeklyPlanRepository plans;
    private final WeeklyCheckInRepository checkIns;
    private final AuthService auth;
    private final RoadmapService roadmaps;
    private final WeeklyAllocationPolicy allocation;
    private final Clock clock;
    private final com.mentorai.progress.adaptation.AdaptationService adaptations;
    private final jakarta.persistence.EntityManager entities;

    public WeeklyProgressService(
            WeeklyPlanRepository plans,
            WeeklyCheckInRepository checkIns,
            AuthService auth,
            RoadmapService roadmaps,
            WeeklyAllocationPolicy allocation,
            Clock clock, com.mentorai.progress.adaptation.AdaptationService adaptations, jakarta.persistence.EntityManager entities) {
        this.plans = plans;
        this.checkIns = checkIns;
        this.auth = auth;
        this.roadmaps = roadmaps;
        this.allocation = allocation;
        this.clock = clock;
        this.adaptations = adaptations;
        this.entities = entities;
    }

    @Transactional
    public WeeklyPlanResponse create(Authentication authentication,CreateWeeklyPlanRequest request){
        UUID owner = auth.requireUser(authentication).getId();
        LocalDate week = currentWeek();
        if (plans.findByUserIdAndWeekStart(owner, week).isPresent()) {
            throw new ConflictException("A weekly plan already exists for this week.");
        }
        RoadmapResponse roadmap = roadmaps.get(authentication, request.roadmapId());
        WeeklyPlan plan = buildPlan(owner, roadmap, week, roadmap.weeklyHours(), Set.of(),
                "Planned from ready roadmap tasks for this week.");
        return describePlan(plans.saveAndFlush(plan), roadmap.revision());
    }

    public WeeklyPlanResponse currentPlan(Authentication authentication,LocalDate requested){
        UUID owner = auth.requireUser(authentication).getId();
        LocalDate week = validatedWeek(requested);
        WeeklyPlan plan = plans.findByUserIdAndWeekStart(owner, week)
                .orElseThrow(() -> new ResourceNotFoundException("Weekly plan was not found."));
        return describePlan(plan, roadmaps.get(authentication, plan.getRoadmapId()).revision());
    }

    public CheckInResponse currentCheckIn(Authentication authentication,LocalDate requested){
        UUID owner = auth.requireUser(authentication).getId();
        LocalDate week = validatedWeek(requested);
        WeeklyPlan plan = plans.findByUserIdAndWeekStart(owner, week)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in was not found."));
        WeeklyCheckIn checkIn = checkIns.findByPlanIdAndUserId(plan.getId(), owner)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in was not found."));
        return describeCheckIn(authentication, plan, checkIn);
    }

    public CheckInHistoryResponse history(Authentication authentication,int page){
        if (page < 0) throw new ProgressValidationException("Page must be zero or greater.");
        UUID owner = auth.requireUser(authentication).getId();
        var result = plans.findByUserIdOrderByWeekStartDesc(owner, PageRequest.of(page, HISTORY_SIZE));
        var items = result.getContent().stream().map(plan -> new CheckInHistoryResponse.Item(
                describePlan(plan, roadmaps.get(authentication, plan.getRoadmapId()).revision()),
                checkIns.findByPlanIdAndUserId(plan.getId(), owner)
                        .map(checkIn -> describeCheckIn(authentication, plan, checkIn)).orElse(null))).toList();
        return new CheckInHistoryResponse(items, page, result.hasNext());
    }

    @Transactional
    public CheckInResponse submit(Authentication authentication,CheckInRequest request){
        UUID owner=auth.requireUser(authentication).getId();
        WeeklyPlan plan=plans.findByIdAndUserId(request.planId(),owner).orElseThrow(()->new ResourceNotFoundException("Weekly plan was not found."));
        RoadmapResponse before=roadmaps.lockForUpdate(authentication,plan.getRoadmapId(),request.expectedRoadmapRevision());
        entities.refresh(plan);
        long expectedPlanRevision=request.expectedPlanRevision()==null?0:request.expectedPlanRevision();
        if(plan.getRevision()!=expectedPlanRevision)throw new ConflictException("This weekly allocation changed. Refresh it before recording your check-in.");
        if(plan.getWeekStart().isAfter(currentWeek()))throw new ProgressValidationException("Future plans cannot be checked in.");
        if(checkIns.existsByPlanId(plan.getId()))throw new ConflictException("This weekly check-in was already submitted.");
        validateConstraint(request.constraint());
        Map<UUID,CheckInRequest.TaskOutcome> submitted=uniqueOutcomes(request.tasks());
        Set<UUID> planned=plan.getTasks().stream().map(WeeklyPlanTask::getTaskId).collect(Collectors.toSet());
        if(!submitted.keySet().equals(planned))throw new ProgressValidationException("Provide exactly one outcome for every planned task.");
        Map<UUID,RoadmapResponse.Task> roadmapTasks=before.phases().stream().flatMap(p->p.tasks().stream()).collect(Collectors.toMap(RoadmapResponse.Task::id,Function.identity()));
        if(!roadmapTasks.keySet().containsAll(planned))throw new ProgressValidationException("Every outcome task must belong to the plan's roadmap.");
        List<UpdateRoadmapRequest.TaskEdit> edits=new ArrayList<>(); Set<UUID> deferred=new HashSet<>();
        for(WeeklyPlanTask task:plan.getTasks()){
            Outcome outcome=submitted.get(task.getTaskId()).outcome(); RoadmapResponse.Task live=roadmapTasks.get(task.getTaskId());
            if(!plan.getMode().equals("MAINTENANCE") && (outcome==Outcome.COMPLETED||outcome==Outcome.PARTIAL)){
                TaskState state=outcome==Outcome.COMPLETED?TaskState.COMPLETED:TaskState.IN_PROGRESS;
                edits.add(new UpdateRoadmapRequest.TaskEdit(live.id(),live.title(),live.estimatedHours(),state));
            } else if(outcome==Outcome.DEFERRED) deferred.add(task.getTaskId());
        }
        RoadmapResponse updated=roadmaps.update(authentication,before.id(),new UpdateRoadmapRequest(request.expectedRoadmapRevision(),null,edits));
        LocalDate nextWeek=plan.getWeekStart().plusWeeks(1);
        Optional<WeeklyPlan> existing=plans.findByUserIdAndWeekStart(owner,nextWeek);
        String explanation; WeeklyPlan next;
        if(existing.isPresent()){
            next=existing.get(); explanation="The existing next-week plan was preserved unchanged because that snapshot already existed.";
        }else{
            next=plans.save(buildPlan(owner,updated,nextWeek,request.availableHoursNextWeek(),deferred,
                    request.availableHoursNextWeek()==0?"A restful zero-hour week chosen by the student.":"Allocated from the student's stated next-week capacity."));
            explanation=request.availableHoursNextWeek()==0?"Next week is intentionally a restful zero-hour plan.":"Next week was planned from ready unfinished work and the stated capacity.";
        }
        WeeklyCheckIn checkIn=new WeeklyCheckIn(plan.getId(),owner,plan.getRoadmapId(),request.actualHours(),request.availableHoursNextWeek(),
                request.difficultyRating(),request.confidenceRating(),request.energyOrCapacityBand(),request.blockers(),normalize(request.notes()),
                next.getId(),explanation,clock.instant());
        for(WeeklyPlanTask task:plan.getTasks())checkIn.addTask(task.getTaskId(),task.getTitle(),submitted.get(task.getTaskId()).outcome());
        if(request.constraint()!=null)checkIn.setConstraint(request.constraint().type(),request.constraint().startDate(),request.constraint().endDate());
        checkIns.saveAndFlush(checkIn);
        adaptations.propose(plan,next,checkIn,updated);
        return describeCheckIn(authentication,plan,checkIn);
    }

    private WeeklyPlan buildPlan(UUID owner,RoadmapResponse roadmap,LocalDate week,int capacity,Set<UUID> excluded,String reason){
        WeeklyPlan plan=new WeeklyPlan(owner,roadmap.id(),roadmap.title(),week,capacity,roadmap.revision(),reason,clock.instant());
        allocation.allocate(roadmap,capacity,excluded).forEach(a->plan.addTask(a.taskId(),a.title(),a.hours())); return plan;
    }
    private Map<UUID,CheckInRequest.TaskOutcome> uniqueOutcomes(List<CheckInRequest.TaskOutcome> tasks){
        Map<UUID,CheckInRequest.TaskOutcome> result=new LinkedHashMap<>();
        for(var task:tasks)if(result.putIfAbsent(task.taskId(),task)!=null)throw new ProgressValidationException("Each planned task must occur exactly once.");
        return result;
    }
    private void validateConstraint(CheckInRequest.Constraint constraint){
        if(constraint==null)return;
        if(constraint.endDate().isBefore(constraint.startDate())||ChronoUnit.DAYS.between(constraint.startDate(),constraint.endDate())>365)
            throw new ProgressValidationException("Constraint dates must be ordered and span no more than 366 days.");
    }
    private LocalDate validatedWeek(LocalDate requested){
        LocalDate week=requested==null?currentWeek():requested;
        if(week.getDayOfWeek()!=DayOfWeek.MONDAY||week.isAfter(currentWeek()))throw new ProgressValidationException("Week start must be a Monday no later than the current week.");
        return week;
    }
    private LocalDate currentWeek(){return LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));}
    private String normalize(String value){return value==null||value.isBlank()?null:value.strip();}
    private WeeklyPlanResponse describePlan(WeeklyPlan plan,long liveRevision){return new WeeklyPlanResponse(plan.getId(),plan.getRoadmapId(),plan.getRoadmapTitle(),plan.getWeekStart(),
            plan.getCapacityHours(),plan.getPlannedHours(),liveRevision,plan.getTasks().stream().map(t->new WeeklyPlanResponse.WeeklyTask(t.getTaskId(),t.getTitle(),t.getPlannedHours())).toList(),plan.getReason(),plan.getCreatedAt(),plan.getRevision(),plan.getMode());}
    private CheckInResponse describeCheckIn(Authentication authentication,WeeklyPlan plan,WeeklyCheckIn checkIn){
        WeeklyPlan next=plans.findByIdAndUserId(checkIn.getNextPlanId(),plan.getUserId()).orElseThrow(()->new ResourceNotFoundException("Next weekly plan was not found."));
        var c=checkIn.getConstraint(); return new CheckInResponse(checkIn.getId(),plan.getId(),plan.getRoadmapId(),plan.getWeekStart(),plan.getPlannedHours(),checkIn.getActualHours(),
                checkIn.getAvailableHoursNextWeek(),checkIn.getDifficultyRating(),checkIn.getConfidenceRating(),checkIn.getEnergyBand(),List.copyOf(checkIn.getBlockers()),checkIn.getNotes(),
                c==null?null:new CheckInResponse.Constraint(c.getType(),c.getStartDate(),c.getEndDate()),checkIn.getTasks().stream().map(t->new CheckInResponse.Task(t.getTaskId(),t.getTitle(),t.getOutcome())).toList(),
                describePlan(next,roadmaps.get(authentication,next.getRoadmapId()).revision()),checkIn.getExplanation(),checkIn.getCreatedAt(),adaptations.forCheckIn(authentication,checkIn.getId()));
    }
}
