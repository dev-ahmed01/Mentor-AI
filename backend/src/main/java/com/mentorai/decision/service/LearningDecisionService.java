package com.mentorai.decision.service;

import com.mentorai.career.entity.CareerSkill;
import com.mentorai.career.entity.SkillRequirement;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.decision.service.LearningPriorityPolicy.Candidate;
import com.mentorai.profile.service.ProfileService;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.service.SkillDependencyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LearningDecisionService {
    private final CareerRepository careers;
    private final ProfileService profiles;
    private final SkillDependencyService dependencies;
    private final LearningPriorityPolicy policy;

    public LearningDecisionService(CareerRepository careers, ProfileService profiles,
                                   SkillDependencyService dependencies, LearningPriorityPolicy policy) {
        this.careers = careers;
        this.profiles = profiles;
        this.dependencies = dependencies;
        this.policy = policy;
    }

    public LearningPrioritiesResponse priorities(UUID careerId, Authentication authentication) {
        var career = careers.findByIdAndActiveTrue(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career was not found."));
        var profile = profiles.get(authentication);
        Map<UUID, SkillProficiency> known = profile.skills().stream()
                .collect(Collectors.toMap(item -> item.id(), item -> item.proficiency()));
        Map<UUID, CareerSkill> direct = career.getSkills().stream()
                .collect(Collectors.toMap(item -> item.getSkill().getId(), Function.identity()));
        List<SkillPrerequisitesResponse> readiness = dependencies.forCareerWithFoundations(careerId, authentication);
        Map<UUID, SkillPrerequisitesResponse> byId = readiness.stream()
                .collect(Collectors.toMap(SkillPrerequisitesResponse::skillId, Function.identity()));
        List<Candidate> candidates = new ArrayList<>();
        for (var item : readiness) {
            CareerSkill own = direct.get(item.skillId());
            SkillProficiency current = known.get(item.skillId());
            var dependents = career.getSkills().stream().filter(target ->
                    !atLeast(known.get(target.getSkill().getId()), SkillProficiency.INTERMEDIATE)
                    && byId.get(target.getSkill().getId()).prerequisites().stream()
                    .anyMatch(prerequisite -> prerequisite.skillId().equals(item.skillId()))).toList();
            int bottlenecks = atLeast(current, SkillProficiency.BEGINNER) ? 0 : dependents.size();
            // Foundation-only nodes still inherit relevance when their downstream skill is already known.
            var allDependents = career.getSkills().stream().filter(target ->
                    byId.get(target.getSkill().getId()).prerequisites().stream()
                    .anyMatch(prerequisite -> prerequisite.skillId().equals(item.skillId()))).toList();
            boolean requiredFoundation = (own == null ? allDependents : dependents).stream()
                    .anyMatch(target -> target.getRequirement() == SkillRequirement.REQUIRED);
            String relevance = own != null && own.getRequirement() == SkillRequirement.REQUIRED ? "REQUIRED"
                    : requiredFoundation && (own == null || bottlenecks > 0) ? "REQUIRED_FOUNDATION"
                    : own != null ? "PREFERRED" : "PREFERRED_FOUNDATION";
            int importance = own == null ? allDependents.stream().mapToInt(CareerSkill::getImportance).max().orElse(1)
                    : relevance.equals("REQUIRED_FOUNDATION")
                    ? Math.max(own.getImportance(), dependents.stream().mapToInt(CareerSkill::getImportance).max().orElse(1))
                    : own.getImportance();
            candidates.add(new Candidate(item, current, own == null ? SkillProficiency.BEGINNER : SkillProficiency.INTERMEDIATE,
                    relevance, importance, bottlenecks));
        }
        return new LearningPrioritiesResponse(career.getId(), career.getName(), LearningPriorityPolicy.VERSION,
                "DEMO DATA", profile.timeAvailablePerWeek(), policy.focusSlots(profile.timeAvailablePerWeek()),
                "UNAVAILABLE", 0, policy.decide(candidates, profile.timeAvailablePerWeek()));
    }

    private boolean atLeast(SkillProficiency actual, SkillProficiency target) {
        return actual != null && actual.ordinal() >= target.ordinal();
    }
}
