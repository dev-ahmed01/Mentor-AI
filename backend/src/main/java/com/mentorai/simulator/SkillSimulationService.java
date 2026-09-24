package com.mentorai.simulator;

import com.mentorai.career.entity.Career;
import com.mentorai.career.entity.SkillRequirement;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.decision.dto.LearningDecision;
import com.mentorai.decision.service.LearningDecisionService;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.repository.SkillRepository;
import com.mentorai.skills.service.SkillDependencyService;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.mentorai.simulator.SkillSimulationResponse.*;

@Service @Transactional(readOnly=true)
public class SkillSimulationService {
    public static final String VERSION="skill-simulation-v1";
    private final ProfileService profiles;
    private final CareerRepository careers;
    private final SkillRepository skills;
    private final LearningDecisionService decisions;
    private final SkillDependencyService dependencies;

    public SkillSimulationService(ProfileService profiles, CareerRepository careers, SkillRepository skills,
                                  LearningDecisionService decisions, SkillDependencyService dependencies) {
        this.profiles=profiles; this.careers=careers; this.skills=skills; this.decisions=decisions; this.dependencies=dependencies;
    }

    public SkillSimulationResponse simulate(Authentication authentication, SkillSimulationRequest request) {
        var profile=profiles.get(authentication);
        var career=careers.findByIdAndActiveTrue(request.targetCareerId()).orElseThrow(() -> new ResourceNotFoundException("Career was not found."));
        var skill=skills.findById(request.skillId()).orElseThrow(() -> new ResourceNotFoundException("Skill was not found."));
        Map<UUID,SkillProficiency> recorded=profile.skills().stream().collect(Collectors.toUnmodifiableMap(t -> t.id(),t -> t.proficiency()));
        Map<UUID,SkillProficiency> hypothetical=new HashMap<>(recorded);
        SkillProficiency current=recorded.get(skill.getId());
        SkillProficiency assumed=current==null || current.ordinal()<SkillProficiency.INTERMEDIATE.ordinal() ? SkillProficiency.INTERMEDIATE : current;
        hypothetical.put(skill.getId(),assumed);
        State before=state(career,recorded,profile.timeAvailablePerWeek());
        State after=state(career,Map.copyOf(hypothetical),profile.timeAvailablePerWeek());
        var satisfied=career.getSkills().stream().filter(t -> !meets(recorded.get(t.getSkill().getId())) && meets(hypothetical.get(t.getSkill().getId())))
                .map(t -> new Requirement(t.getSkill().getId(),t.getSkill().getName(),t.getRequirement()))
                .sorted(Comparator.comparing(Requirement::name).thenComparing(Requirement::skillId)).toList();
        var beforeDecisions=before.priorities().decisions().stream().collect(Collectors.toMap(LearningDecision::skillId,Function.identity()));
        var unlocked=after.priorities().decisions().stream().filter(t -> t.prerequisiteReadiness().eligible()
                        && !t.reasonCodes().contains("ALREADY_PROFICIENT") && !beforeDecisions.get(t.skillId()).prerequisiteReadiness().eligible())
                .map(t -> new UnlockedSkill(t.skillId(),t.name())).sorted(Comparator.comparing(UnlockedSkill::name).thenComparing(UnlockedSkill::skillId)).toList();
        return new SkillSimulationResponse(VERSION,"SIMULATION","DEMO DATA","UNAVAILABLE",career.getId(),career.getName(),skill.getId(),skill.getName(),
                current,assumed,profile.updatedAt(),"Assume only the selected skill reaches at least Intermediate; keep higher recorded proficiency and every other skill unchanged. Prerequisites are not automatically learned. This preview uses your recorded profile, not roadmap completion or market evidence.",
                dependencies.prerequisites(skill.getId(),recorded),before,after,satisfied,unlocked,before.equals(after));
    }

    private State state(Career career,Map<UUID,SkillProficiency> known,Integer hours) {
        int required=0,requiredMet=0,preferred=0,preferredMet=0;
        for(var requirement:career.getSkills()) {
            boolean satisfied=meets(known.get(requirement.getSkill().getId()));
            if(requirement.getRequirement()==SkillRequirement.REQUIRED) { required++; if(satisfied)requiredMet++; }
            else { preferred++; if(satisfied)preferredMet++; }
        }
        var priorities=decisions.priorities(career.getId(),known,hours);
        int eligible=(int)priorities.decisions().stream().filter(t -> t.prerequisiteReadiness().eligible() && !t.reasonCodes().contains("ALREADY_PROFICIENT")).count();
        return new State(requiredMet,required,preferredMet,preferred,eligible,priorities);
    }
    private boolean meets(SkillProficiency level) { return level!=null && level.ordinal()>=SkillProficiency.INTERMEDIATE.ordinal(); }
}
