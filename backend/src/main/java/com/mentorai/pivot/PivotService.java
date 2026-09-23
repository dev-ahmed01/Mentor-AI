package com.mentorai.pivot;

import com.mentorai.auth.repository.UserRepository;
import com.mentorai.auth.service.AuthService;
import com.mentorai.common.exception.*;
import com.mentorai.decision.service.LearningDecisionService;
import com.mentorai.profile.dto.ProfileResponse;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.roadmap.entity.RoadmapTask;
import com.mentorai.roadmap.repository.RoadmapRepository;
import com.mentorai.roadmap.service.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.mentorai.pivot.PivotModels.*;

@Service @Transactional(readOnly=true)
public class PivotService {
    private final AuthService auth; private final UserRepository users; private final ProfileService profiles;
    private final RoadmapRepository roadmaps; private final RoadmapService roadmapService;
    private final RoadmapGenerator generator; private final LearningDecisionService decisions;
    private final PivotPolicy policy; private final PivotRepository pivots;
    private final com.mentorai.roadmap.repository.RoadmapCreditRepository taskCredits;
    public PivotService(AuthService auth,UserRepository users,ProfileService profiles,RoadmapRepository roadmaps,
            RoadmapService roadmapService,RoadmapGenerator generator,LearningDecisionService decisions,PivotPolicy policy,PivotRepository pivots,
            com.mentorai.roadmap.repository.RoadmapCreditRepository taskCredits){
        this.auth=auth;this.users=users;this.profiles=profiles;this.roadmaps=roadmaps;this.roadmapService=roadmapService;
        this.generator=generator;this.decisions=decisions;this.policy=policy;this.pivots=pivots;
        this.taskCredits=taskCredits;
    }
    public Detail get(Authentication authentication,UUID id){return pivots.get(id,auth.requireUser(authentication).getId()).detail();}
    public List<Summary> list(Authentication authentication){return pivots.list(auth.requireUser(authentication).getId());}

    @Transactional
    public Detail create(Authentication authentication,CreateRequest request){
        UUID owner=lockOwner(authentication);
        RoadmapResponse source=lockSource(owner,request.sourceRoadmapId());
        requireCurrent(owner,source.id());
        if(source.careerId().equals(request.targetCareerId()))throw new RoadmapValidationException("Choose a different career to compare.");
        ProfileResponse profile=profiles.get(authentication);
        if(profile.timeAvailablePerWeek()==null)throw new ProfileIncompleteException("Record weekly learning availability before comparing a career pivot.");
        var credits=policy.credits(source,taskCredits.forRoadmap(source.id()));var known=policy.effective(profile,credits);
        var before=decisions.priorities(source.careerId(),known,profile.timeAvailablePerWeek());
        var after=decisions.priorities(request.targetCareerId(),known,profile.timeAvailablePerWeek());
        var draft=generator.generate(owner,after,profile.updatedAt(),source.id());
        var byId=draft.getPhases().stream().flatMap(p->p.getTasks().stream()).collect(Collectors.toMap(RoadmapTask::getId,Function.identity()));
        var stages=draft.getPhases().stream().map(p->new Stage(p.getTitle(),p.getTasks().stream().map(t->new ProposedTask(t.getSkillId(),t.getSkillName(),t.getEstimatedHours(),t.isSatisfiedAtGeneration(),t.getPrerequisites().keySet().stream().map(id->byId.get(id).getSkillName()).sorted().toList())).toList())).toList();
        var comparison=policy.compare(source,profile,credits,before,after,stages);
        Detail detail=new Detail(UUID.randomUUID(),now(),"PREVIEW",null,null,comparison);
        pivots.create(owner,detail,fingerprint(profile));return detail;
    }

    @Transactional
    public Detail accept(Authentication authentication,UUID id,AcceptRequest request){
        UUID owner=lockOwner(authentication);
        Stored stored=pivots.get(id,owner);Detail detail=stored.detail();var comparison=detail.comparison();
        var savedSource=comparison.sourceRoadmap();
        if(request.expectedSourceRevision()!=savedSource.revision())throw stale();
        // Retry is resolved after the owner lock and fresh JDBC read, even if another pivot happened later.
        if(detail.acceptedRoadmapId()!=null)return detail;
        RoadmapResponse source=lockSource(owner,savedSource.id());
        requireCurrent(owner,source.id());
        ProfileResponse profile=profiles.get(authentication);
        if(source.revision()!=savedSource.revision() || !fingerprint(profile).equals(stored.profileFingerprint()))throw stale();
        var known=policy.effective(profile,policy.credits(source,taskCredits.forRoadmap(source.id())));
        var before=decisions.priorities(source.careerId(),known,profile.timeAvailablePerWeek());
        var after=decisions.priorities(comparison.after().careerId(),known,profile.timeAvailablePerWeek());
        if(!before.equals(comparison.before()) || !after.equals(comparison.after()) || !PivotPolicy.VERSION.equals(comparison.policyVersion()))throw stale();
        var revised=generator.generate(owner,comparison.after(),comparison.profileUpdatedAt(),source.id());
        roadmaps.saveAndFlush(revised);
        revised.getPhases().stream().flatMap(p->p.getTasks().stream()).filter(RoadmapTask::isSatisfiedAtGeneration)
                .forEach(t->taskCredits.retain(t.getId(),known.get(t.getSkillId())));
        pivots.accept(id,owner,revised.getId(),now());
        return pivots.get(id,owner).detail();
    }
    private UUID lockOwner(Authentication authentication){UUID owner=auth.requireUser(authentication).getId();users.lockById(owner).orElseThrow();return owner;}
    private RoadmapResponse lockSource(UUID owner,UUID id){return roadmapService.describe(roadmaps.findOwnedForUpdate(id,owner).orElseThrow(()->new ResourceNotFoundException("Roadmap was not found.")));}
    private void requireCurrent(UUID owner,UUID id){if(!roadmaps.findFirstByUserIdOrderByCreatedAtDescIdDesc(owner).map(r->r.getId().equals(id)).orElse(false))throw new ConflictException("Your current roadmap changed. Create a new pivot preview from your current roadmap.");}
    private ConflictException stale(){return new ConflictException("This preview is stale. Your profile, source roadmap or career requirements changed. Create a new preview before accepting.");}
    private Instant now(){return Instant.now().truncatedTo(ChronoUnit.MICROS);}
    private String fingerprint(ProfileResponse profile){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pivots.encode(profile).getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
