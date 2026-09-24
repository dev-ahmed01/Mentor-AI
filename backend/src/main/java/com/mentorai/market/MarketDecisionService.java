package com.mentorai.market;

import com.mentorai.auth.service.AuthService;
import com.mentorai.career.dto.CareerCandidateResponse;
import com.mentorai.career.dto.CareerSkillResponse;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.career.service.CareerFitScoringService;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.decision.service.LearningDecisionService;
import com.mentorai.profile.repository.StudentProfileRepository;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.entity.SkillConfidence;
import com.mentorai.market.MarketModels.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketDecisionService {
    public static final String VERSION="career-market-v1";
    public record SkillInput(UUID id,String normalizedName,SkillProficiency proficiency,SkillConfidence confidence) { }
    public record ProfileInputs(List<String> interests,List<String> goals,List<String> programmingLanguages,
                                List<String> preferredDomains,String shortTermGoal,String longTermGoal,List<SkillInput> skills) { }
    public record CatalogInputs(String entryDifficulty,int recommendedWeeklyHours,List<String> interestSignals,
                                List<String> goalSignals,List<CareerSkillResponse> skills) { }
    public record Decision(UUID id,Instant calculatedAt,String calculationVersion,Instant profileUpdatedAt,
                           String profileScoringVersion,ProfileInputs profileInputs,CatalogInputs catalogInputs,
                           Map<UUID,SkillProficiency> recordedSkills,Integer weeklyHours,Evidence evidence,
                           CareerCandidateResponse profileOnlyCareer,Integer marketCompatibility,int marketWeight,
                           int careerFitIndicator,LearningPrioritiesResponse priorities,String methodology) { }
    private final MarketRepository repository;
    private final MarketAnalyticsService analytics;
    private final AuthService auth;
    private final StudentProfileRepository profiles;
    private final CareerRepository careers;
    private final CareerFitScoringService scoring;
    private final LearningDecisionService decisions;
    public MarketDecisionService(MarketRepository repository,MarketAnalyticsService analytics,AuthService auth,
                                 StudentProfileRepository profiles,CareerRepository careers,CareerFitScoringService scoring,LearningDecisionService decisions) {
        this.repository=repository;this.analytics=analytics;this.auth=auth;this.profiles=profiles;this.careers=careers;this.scoring=scoring;this.decisions=decisions;
    }
    @Transactional
    public Decision create(UUID snapshotId,Authentication authentication) {
        var user=auth.requireUser(authentication);
        var profile=profiles.findByUserId(user.getId()).orElseThrow(()->new ResourceNotFoundException("Profile was not found."));
        var snapshot=repository.snapshot(snapshotId);
        var career=careers.findByIdAndActiveTrue(snapshot.careerId()).orElseThrow(()->new ResourceNotFoundException("Career was not found."));
        Instant now=Instant.now();
        var evidence=analytics.evidence(snapshot,now);
        boolean eligible=evidence.status().equals("AVAILABLE");
        var base=scoring.score(profile,List.of(career),1).candidates().getFirst();
        Map<UUID,SkillProficiency> known=profile.getSkills().stream().collect(Collectors.toMap(s->s.getSkill().getId(),s->s.getProficiency()));
        Map<UUID,Integer> bonuses=new HashMap<>();
        double achieved=0,total=0;
        for(var skill:snapshot.skills()) {
            total+=skill.mentions();
            var proficiency=known.get(skill.skillId());
            achieved+=skill.mentions()*(proficiency==null?0:Math.min(1,(proficiency.ordinal()+1)/3.0));
            if(eligible)bonuses.put(skill.skillId(),(int)Math.round(10.0*skill.mentions()/snapshot.sampleSize()));
        }
        Integer compatibility=eligible?(int)Math.round(100*achieved/total):null;
        var f=base.factors();
        int indicator=eligible?(int)Math.round((20*f.interestAlignment()+15*f.goalAlignment()+30*f.skillAlignment()
                +15*f.entryAccessibility()+5*f.learningEffortCompatibility()+15*compatibility)/100.0):base.careerFitIndicator();
        var priorities=decisions.priorities(career.getId(),known,profile.getTimeAvailablePerWeek(),bonuses);
        var inputs=new ProfileInputs(List.copyOf(profile.getInterests()),List.copyOf(profile.getGoals()),List.copyOf(profile.getProgrammingLanguages()),
                List.copyOf(profile.getPreferredDomains()),profile.getShortTermGoal(),profile.getLongTermGoal(),profile.getSkills().stream()
                .map(s->new SkillInput(s.getSkill().getId(),s.getSkill().getNormalizedName(),s.getProficiency(),s.getConfidence()))
                .sorted(Comparator.comparing(SkillInput::id)).toList());
        var catalogInputs=new CatalogInputs(career.getEntryDifficulty().name(),career.getRecommendedWeeklyHours(),
                career.getInterestSignals().stream().sorted().toList(),career.getGoalSignals().stream().sorted().toList(),
                career.getSkills().stream().map(CareerSkillResponse::from).sorted(Comparator.comparing(CareerSkillResponse::id)).toList());
        var result=new Decision(UUID.randomUUID(),now,VERSION,profile.getUpdatedAt(),CareerFitScoringService.CALCULATION_VERSION,inputs,catalogInputs,Map.copyOf(known),profile.getTimeAvailablePerWeek(),
                evidence,base,compatibility,eligible?15:0,indicator,priorities,
                "Career score: existing profile factors (20 interest + 15 goal + 30 skill + 15 accessibility + 5 effort), plus 15 market points when eligible, divided by 100; otherwise profile-only /85. Market compatibility is frequency-weighted coverage of catalog mentions: missing=0, Awareness=1/3, Beginner=2/3, Intermediate/Advanced=1. Skill ordering adds round(10*mentionCount/sampleSize), capped at 100, while preserving readiness and time gates. All mention categories count once per listing. This is source-specific alignment, not hiring probability. Catalog factors remain DEMO DATA.");
        repository.decision(result.id(),user.getId(),snapshotId,now,VERSION,result);
        return result;
    }
    @Transactional(readOnly=true)
    public Decision get(UUID id,Authentication authentication) {
        return repository.decode(repository.decision(id,auth.requireUser(authentication).getId()),Decision.class);
    }
}
