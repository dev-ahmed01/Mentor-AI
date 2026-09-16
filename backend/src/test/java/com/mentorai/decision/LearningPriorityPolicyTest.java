package com.mentorai.decision;

import static org.assertj.core.api.Assertions.assertThat;

import com.mentorai.decision.dto.LearningPriority;
import com.mentorai.decision.service.LearningPriorityPolicy;
import com.mentorai.decision.service.LearningPriorityPolicy.Candidate;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.entity.SkillProficiency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningPriorityPolicyTest {
    private final LearningPriorityPolicy policy = new LearningPriorityPolicy();

    @Test
    void requiredOutranksPreferredWhenOtherFactorsAreEqual() {
        var result = policy.decide(List.of(candidate("A preferred", "PREFERRED", null),
                candidate("Z required", "REQUIRED", null)), 2);
        assertThat(result.getFirst().name()).isEqualTo("Z required");
        assertThat(result.getFirst().priority()).isEqualTo(LearningPriority.LEARN_NOW);
        assertThat(result.getLast().priority()).isEqualTo(LearningPriority.LEARN_NEXT);
        assertThat(result.getFirst().deterministicScore()).isGreaterThan(result.getLast().deterministicScore());
    }

    @Test
    void stableTiesDoNotDependOnInputOrderAndProficientSkillsConsumeNoSlots() {
        var a = candidate("A", "REQUIRED", null);
        var z = candidate("Z", "REQUIRED", null);
        var known = candidate("Known", "REQUIRED", SkillProficiency.ADVANCED);
        var result = policy.decide(List.of(z, known, a), 1);
        assertThat(result).isEqualTo(policy.decide(List.of(a, z, known), 1));
        assertThat(result.getFirst().name()).isEqualTo("A");
        assertThat(result.getFirst().priority()).isEqualTo(LearningPriority.LEARN_NOW);
        assertThat(result.getLast().reasonCodes()).contains("ALREADY_PROFICIENT");
    }

    @Test
    void capacityBoundariesAndUnknownTimeAreExplicit() {
        assertThat(policy.focusSlots(null)).isZero();
        assertThat(policy.focusSlots(0)).isZero();
        assertThat(policy.focusSlots(1)).isEqualTo(1);
        assertThat(policy.focusSlots(4)).isEqualTo(1);
        assertThat(policy.focusSlots(5)).isEqualTo(2);
        assertThat(policy.focusSlots(9)).isEqualTo(2);
        assertThat(policy.focusSlots(10)).isEqualTo(3);
        assertThat(policy.focusSlots(168)).isEqualTo(3);
        var result = policy.decide(List.of(candidate("A", "REQUIRED", null)), null).getFirst();
        assertThat(result.priority()).isEqualTo(LearningPriority.LEARN_NEXT);
        assertThat(result.reasonCodes()).contains("WEEKLY_TIME_UNAVAILABLE");
    }

    private Candidate candidate(String name, String relevance, SkillProficiency current) {
        var readiness = new SkillPrerequisitesResponse(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                name, true, "NO_RECORDED_PREREQUISITES", SkillProficiency.BEGINNER,
                "skill-prerequisites-v1", "DEMO DATA", List.of());
        return new Candidate(readiness, current, SkillProficiency.INTERMEDIATE, relevance, 4, 0);
    }
}
