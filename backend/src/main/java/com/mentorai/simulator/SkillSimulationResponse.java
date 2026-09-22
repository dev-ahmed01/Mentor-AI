package com.mentorai.simulator;

import com.mentorai.career.entity.SkillRequirement;
import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.entity.SkillProficiency;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SkillSimulationResponse(String calculationVersion, String resultLabel, String dataLabel,
        String marketEvidenceStatus, UUID targetCareerId, String careerName, UUID skillId, String skillName,
        SkillProficiency recordedProficiency, SkillProficiency assumedProficiency, Instant profileUpdatedAt,
        String assumption, SkillPrerequisitesResponse selectedSkillPrerequisites, State before, State after,
        List<Requirement> newlySatisfiedRequirements, List<UnlockedSkill> newlyEligibleSkills, boolean noChange) {
    public record State(int requiredSatisfied, int requiredTotal, int preferredSatisfied, int preferredTotal,
                        int eligibleUnfinishedSkills, LearningPrioritiesResponse priorities) { }
    public record Requirement(UUID skillId, String name, SkillRequirement requirement) { }
    public record UnlockedSkill(UUID skillId, String name) { }
}
