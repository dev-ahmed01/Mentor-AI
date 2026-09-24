package com.mentorai.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mentorai.auth.service.AuthService;
import com.mentorai.auth.repository.UserRepository;
import com.mentorai.common.exception.*;
import com.mentorai.profile.dto.*;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.profile.entity.RemotePreference;
import com.mentorai.skills.entity.*;
import com.mentorai.roadmap.dto.*;
import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.roadmap.service.RoadmapService;
import com.mentorai.progress.dto.*;
import com.mentorai.progress.entity.ProgressEnums.*;
import com.mentorai.progress.service.WeeklyProgressService;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.mentorai.demo.DemoModels.*;

@Service @Transactional(readOnly=true)
public class DemoService {
    private static final UUID CAREER=UUID.fromString("20000000-0000-0000-0000-000000000001");
    private final boolean enabled;private final AuthService auth;private final UserRepository users;private final ProfileService profiles;
    private final RoadmapService roadmaps;private final WeeklyProgressService progress;private final DemoRepository demos;private final ObjectMapper json;private final Clock clock;
    public DemoService(@Value("${mentorai.demo.enabled:false}") boolean enabled,AuthService auth,UserRepository users,
            ProfileService profiles,RoadmapService roadmaps,WeeklyProgressService progress,DemoRepository demos,ObjectMapper json,Clock clock){
        this.enabled=enabled;this.auth=auth;this.users=users;this.profiles=profiles;this.roadmaps=roadmaps;this.progress=progress;this.demos=demos;this.json=json;this.clock=clock;
    }
    public Status status(Authentication authentication){return new Status(enabled,demos.get(auth.requireUser(authentication).getId()).orElse(null),null);}
    @Transactional
    public Status start(Authentication authentication){
        requireEnabled();UUID owner=lockOwner(authentication);var existing=demos.get(owner);
        if(existing.isPresent())return new Status(true,existing.get(),null);
        ObjectNode profile=json.valueToTree(profiles.get(authentication));profile.remove(List.of("id","updatedAt"));
        boolean populated=false;for(var value:profile)if(!value.isNull() && !(value.isArray() && value.isEmpty())){populated=true;break;}
        if(populated||demos.hasHistory(owner))throw new ConflictException("Use a new empty account for the synthetic demo. Existing profiles and history are never replaced.");
        profiles.update(authentication,new UpdateProfileRequest("BCA",2,3,List.of("Backend development"),List.of("Build a backend portfolio"),List.of("Java"),List.of("Backend"),List.of(),RemotePreference.FLEXIBLE,8,
                List.of(skill("Java",SkillProficiency.BEGINNER),skill("SQL",SkillProficiency.INTERMEDIATE),skill("Git",SkillProficiency.BEGINNER)),
                List.of("SYNTHETIC: student library API"),"SYNTHETIC DEMO persona; not a real student's history.",List.of(),"Build foundations at eight hours per week","Explore backend development",List.of()));
        var roadmap=roadmaps.create(authentication,new CreateRoadmapRequest(CAREER));
        var git=roadmap.phases().stream().flatMap(p->p.tasks().stream()).filter(t->t.skillName().equals("Git")).findFirst().orElseThrow();
        roadmap=roadmaps.update(authentication,roadmap.id(),new UpdateRoadmapRequest(roadmap.revision(),"SYNTHETIC DEMO — Backend learning path",List.of(new UpdateRoadmapRequest.TaskEdit(git.id(),git.title(),git.estimatedHours(),TaskState.COMPLETED))));
        var plan=progress.create(authentication,new CreateWeeklyPlanRequest(roadmap.id()));
        var run=new Run(UUID.randomUUID(),roadmap.id(),plan.id(),plan.weekStart(),roadmap.revision(),plan.revision(),"hackathon-demo-v1",clock.instant().truncatedTo(ChronoUnit.MICROS),null);
        demos.save(owner,run);return new Status(true,run,null);
    }
    @Transactional
    public Status exam(Authentication authentication,ExamRequest request){
        requireEnabled();UUID owner=lockOwner(authentication);
        var run=demos.get(owner).orElseThrow(()->new ResourceNotFoundException("Start your synthetic demo before recording the exam scenario."));
        if(run.examCheckInId()!=null)return new Status(true,run,progress.currentCheckIn(authentication,run.weekStart()));
        if(!run.weekStart().equals(LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))) || !roadmaps.current(authentication).id().equals(run.roadmapId())
                || request.expectedRoadmapRevision()!=run.roadmapRevision() || request.expectedPlanRevision()!=run.planRevision())
            throw new ConflictException("This demo week or roadmap changed. Rehearse in a new empty account, or use the regular check-in form.");
        var plan=progress.currentPlan(authentication,run.weekStart());
        if(!plan.id().equals(run.planId()))throw new ConflictException("The demo allocation changed. Use the regular check-in form.");
        List<CheckInRequest.TaskOutcome> outcomes=new ArrayList<>();
        for(int i=0;i<plan.tasks().size();i++)outcomes.add(new CheckInRequest.TaskOutcome(plan.tasks().get(i).taskId(),i==0?Outcome.PARTIAL:Outcome.MISSED));
        LocalDate today=LocalDate.now(clock);
        var checkIn=progress.submit(authentication,new CheckInRequest(run.planId(),request.expectedRoadmapRevision(),request.expectedPlanRevision(),2,2,3,3,EnergyBand.LOW,List.of(Blocker.NO_TIME),
                "SYNTHETIC DEMO: exam preparation reduced this week's learning time.",new CheckInRequest.Constraint(ConstraintType.EXAMS,today,today.plusDays(13)),outcomes));
        demos.recordExam(owner,checkIn.id());return new Status(true,demos.get(owner).orElseThrow(),checkIn);
    }
    private ProfileSkillRequest skill(String name,SkillProficiency level){return new ProfileSkillRequest(name,"Starter skill",level,SkillConfidence.MEDIUM,SkillSource.SELF_REPORTED);}
    private UUID lockOwner(Authentication authentication){UUID owner=auth.requireUser(authentication).getId();users.lockById(owner).orElseThrow();return owner;}
    private void requireEnabled(){if(!enabled)throw new ResourceNotFoundException("Synthetic demo setup is disabled.");}
}
