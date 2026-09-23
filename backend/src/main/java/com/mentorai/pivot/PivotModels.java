package com.mentorai.pivot;

import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.roadmap.dto.RoadmapResponse;
import com.mentorai.skills.entity.SkillProficiency;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class PivotModels {
    private PivotModels() { }
    public record CreateRequest(@NotNull UUID sourceRoadmapId, @NotNull UUID targetCareerId) { }
    public record AcceptRequest(@NotNull @Min(0) Long expectedSourceRevision) { }
    public record Skill(UUID skillId, String name, SkillProficiency currentProficiency, SkillProficiency targetProficiency) { }
    public record Credit(UUID taskId, UUID skillId, String name, SkillProficiency proficiency, String source) { }
    public record PriorityChange(UUID skillId, String name, String beforePriority, String afterPriority,
                                 Integer beforePoints, Integer afterPoints, String beforeRelevance, String afterRelevance) { }
    public record ProposedTask(UUID skillId, String name, int estimatedHours, boolean targetMet, List<String> prerequisites) { }
    public record Stage(String title, List<ProposedTask> tasks) { }
    public record Effort(String band, int illustrativeHours, int capacityWeeks, String explanation) { }
    public record Comparison(String policyVersion, RoadmapResponse sourceRoadmap, Instant profileUpdatedAt,
            LearningPrioritiesResponse before, LearningPrioritiesResponse after, List<Credit> credits,
            List<Skill> transferableSkills, List<Skill> newlyRequiredSkills, List<Skill> satisfiedPrerequisites,
            List<Skill> skippableSkills, List<PriorityChange> changedPriorities, Effort effort, List<Stage> proposedStages) { }
    public record Detail(UUID id, Instant createdAt, String status, UUID acceptedRoadmapId, Instant acceptedAt, Comparison comparison) { }
    public record Summary(UUID id, Instant createdAt, String sourceCareerName, String targetCareerName, String status, UUID acceptedRoadmapId) { }
    public record Stored(Detail detail, String profileFingerprint) { }
}
