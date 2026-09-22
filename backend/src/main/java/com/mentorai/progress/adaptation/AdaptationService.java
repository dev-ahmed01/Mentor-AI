package com.mentorai.progress.adaptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorai.auth.service.AuthService;
import com.mentorai.common.exception.ConflictException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.progress.entity.*;
import com.mentorai.progress.repository.*;
import com.mentorai.progress.service.ProgressValidationException;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.roadmap.service.RoadmapService;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.mentorai.progress.adaptation.AdaptationResponse.*;

@Service @Transactional(readOnly=true)
public class AdaptationService {
    private final AdaptationRepository revisions;
    private final WeeklyPlanRepository plans;
    private final WeeklyCheckInRepository checkIns;
    private final AuthService auth;
    private final RoadmapService roadmaps;
    private final AdaptationPolicy policy;
    private final ObjectMapper json;
    private final Clock clock;
    private final EntityManager entities;

    public AdaptationService(AdaptationRepository revisions, WeeklyPlanRepository plans, WeeklyCheckInRepository checkIns,
            AuthService auth, RoadmapService roadmaps, AdaptationPolicy policy, ObjectMapper json, Clock clock, EntityManager entities) {
        this.revisions=revisions; this.plans=plans; this.checkIns=checkIns; this.auth=auth; this.roadmaps=roadmaps;
        this.policy=policy; this.json=json; this.clock=clock; this.entities=entities;
    }

    @Transactional
    public void propose(WeeklyPlan source, WeeklyPlan target, WeeklyCheckIn checkIn, RoadmapResponse roadmap) {
        // A late reflection may point to a separately generated roadmap. Never revise that plan.
        if (!source.getRoadmapId().equals(target.getRoadmapId())) return;
        var history = checkIns.historyThrough(source.getUserId(), source.getRoadmapId(), source.getWeekStart());
        var decision = policy.propose(roadmap, source, target, checkIn, history);
        revisions.save(new Adaptation(source.getUserId(), source.getRoadmapId(), checkIn.getId(), target.getId(),
                roadmap.revision(), target.getRevision(), encode(snapshot(target)), encode(decision), clock.instant()));
    }

    public AdaptationResponse forCheckIn(Authentication authentication, UUID checkInId) {
        UUID owner = auth.requireUser(authentication).getId();
        return revisions.findByCheckInIdAndUserId(checkInId, owner).map(r -> describe(authentication, owner, r)).orElse(null);
    }
    public AdaptationResponse get(Authentication authentication, UUID id) {
        UUID owner = auth.requireUser(authentication).getId();
        return describe(authentication, owner, requireOwned(owner, id));
    }
    public History history(Authentication authentication, UUID roadmapId, int page) {
        if (page < 0) throw new ProgressValidationException("Page must be zero or greater.");
        roadmaps.get(authentication, roadmapId);
        UUID owner = auth.requireUser(authentication).getId();
        var result = revisions.findByRoadmapIdAndUserIdOrderByCreatedAtDescIdDesc(roadmapId, owner, PageRequest.of(page,20));
        return new History(result.getContent().stream().map(r -> describe(authentication, owner, r)).toList(), page, result.hasNext());
    }

    @Transactional
    public AdaptationResponse accept(Authentication authentication, UUID id, AcceptAdaptationRequest request) {
        UUID owner = auth.requireUser(authentication).getId();
        Adaptation revision = requireOwned(owner, id);
        RoadmapResponse roadmap = roadmaps.lockForUpdate(authentication, revision.getRoadmapId(), request.expectedRoadmapRevision());
        // Refresh after the aggregate lock: a competing request may have accepted this proposal while we waited.
        entities.refresh(revision);
        WeeklyPlan plan = plans.findByIdAndUserId(revision.getPlanId(), owner).orElseThrow(() -> new ResourceNotFoundException("Weekly plan was not found."));
        entities.refresh(plan);
        if (!canAccept(revision, plan, roadmap.revision()) || request.expectedPlanRevision() != plan.getRevision())
            throw new ConflictException("This proposal is no longer applicable. Refresh your weekly progress before making changes.");
        Decision decision = decode(revision.getDecisionJson(), Decision.class);
        Snapshot chosen = request.edit() == null ? decision.proposed() : edited(request.edit(), decision.proposed().mode(), roadmap);
        validate(chosen, roadmap);
        plan.clearAllocation(chosen.capacityHours(), chosen.mode(), decision.reason());
        // Delete old children before inserting the same positions/task IDs (unique constraints).
        plans.flush();
        chosen.tasks().forEach(t -> plan.addTask(t.taskId(), t.title(), t.plannedHours()));
        revision.accept(encode(chosen), clock.instant());
        plans.flush(); revisions.flush();
        return describe(authentication, owner, revision);
    }

    private Snapshot edited(AcceptAdaptationRequest.Edit edit, String mode, RoadmapResponse roadmap) {
        var byId = tasks(roadmap);
        List<Task> selected = new ArrayList<>();
        for (var task : edit.tasks()) {
            var live = byId.get(task.taskId());
            if (live == null) throw new ProgressValidationException("Select tasks from this roadmap only.");
            selected.add(new Task(live.id(), mode.equals("MAINTENANCE") ? AdaptationPolicy.reviewTitle(live.title()) : live.title(), task.plannedHours()));
        }
        return new Snapshot(edit.capacityHours(), mode, List.copyOf(selected));
    }
    private void validate(Snapshot chosen, RoadmapResponse roadmap) {
        var byId = tasks(roadmap);
        Set<UUID> seen = new HashSet<>(); int total=0;
        boolean maintenance = chosen.mode().equals("MAINTENANCE");
        if (maintenance && (chosen.capacityHours() > 2 || chosen.tasks().size() > 1))
            throw new ProgressValidationException("Maintenance mode allows at most two hours and one review task.");
        for (var task : chosen.tasks()) {
            var live = byId.get(task.taskId());
            if (live == null || !seen.add(task.taskId())) throw new ProgressValidationException("Each selected task must belong to this roadmap and occur once.");
            boolean eligible = maintenance ? live.state() == TaskState.IN_PROGRESS || live.state() == TaskState.NEEDS_REVIEW
                    || live.state() == TaskState.COMPLETED || live.satisfiedAtGeneration() : live.ready() && !live.state().terminal();
            if (!eligible) throw new ProgressValidationException("Selected tasks must be eligible for this plan's learning or review mode.");
            if (task.plannedHours() < 1 || task.plannedHours() > (maintenance ? 2 : live.estimatedHours()))
                throw new ProgressValidationException("Task hours must be positive and within the task estimate or review limit.");
            total += task.plannedHours();
        }
        if (total > chosen.capacityHours()) throw new ProgressValidationException("Selected task hours exceed the weekly capacity.");
    }
    private Map<UUID,RoadmapResponse.Task> tasks(RoadmapResponse roadmap) {
        return roadmap.phases().stream().flatMap(p -> p.tasks().stream()).collect(Collectors.toMap(RoadmapResponse.Task::id, Function.identity()));
    }
    private boolean canAccept(Adaptation revision, WeeklyPlan plan, long roadmapRevision) {
        LocalDate current = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return revision.getStatus().equals("PENDING") && revision.getRoadmapRevision() == roadmapRevision
                && revision.getPlanRevision() == plan.getRevision() && !plan.getWeekStart().isBefore(current)
                && !checkIns.existsByPlanId(plan.getId());
    }
    private AdaptationResponse describe(Authentication authentication, UUID owner, Adaptation revision) {
        WeeklyPlan plan = plans.findByIdAndUserId(revision.getPlanId(), owner).orElseThrow(() -> new ResourceNotFoundException("Weekly plan was not found."));
        Decision decision = decode(revision.getDecisionJson(), Decision.class);
        return new AdaptationResponse(revision.getId(), revision.getCheckInId(), revision.getRoadmapId(), revision.getPlanId(), plan.getWeekStart(),
                revision.getStatus(), revision.getPolicyVersion(), decision.trigger(), decision.reason(), revision.getCreatedAt(), revision.getAcceptedAt(),
                revision.getRoadmapRevision(), revision.getPlanRevision(), canAccept(revision, plan, roadmaps.get(authentication,revision.getRoadmapId()).revision()),
                decode(revision.getBeforeJson(), Snapshot.class), decision.proposed(), revision.getAcceptedJson() == null ? null : decode(revision.getAcceptedJson(), Snapshot.class),
                decision.resumeTaskId(), decision.resumeTitle(), decision.blockerQuestions(), decision.candidates());
    }
    private Snapshot snapshot(WeeklyPlan plan) {
        return new Snapshot(plan.getCapacityHours(), plan.getMode(), plan.getTasks().stream().map(t -> new Task(t.getTaskId(),t.getTitle(),t.getPlannedHours())).toList());
    }
    private Adaptation requireOwned(UUID owner, UUID id) {
        return revisions.findByIdAndUserId(id,owner).orElseThrow(() -> new ResourceNotFoundException("Adaptation was not found."));
    }
    private String encode(Object value) {
        try { return json.writeValueAsString(value); } catch (JsonProcessingException e) { throw new IllegalStateException("Could not record adaptation.",e); }
    }
    private <T> T decode(String value, Class<T> type) {
        try { return json.readValue(value,type); } catch (JsonProcessingException e) { throw new IllegalStateException("Could not read adaptation.",e); }
    }
}
