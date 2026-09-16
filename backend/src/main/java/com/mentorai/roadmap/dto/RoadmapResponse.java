package com.mentorai.roadmap.dto;

import com.mentorai.roadmap.entity.TaskState;
import com.mentorai.skills.entity.SkillProficiency;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoadmapResponse(UUID id, UUID careerId, String careerName, String title, long revision,
                              String generationVersion, String decisionVersion, String dataLabel,
                              Instant createdAt, Instant updatedAt, Instant profileUpdatedAt,
                              UUID previousRoadmapId, int weeklyHours, UUID currentPhaseId,
                              String currentPriority, Task nextAction, List<WeeklyFocus> thisWeek, List<Phase> phases) {
    public record Phase(UUID id, int position, String title, List<Task> tasks) { }
    public record Task(UUID id, UUID skillId, String skillName, String title, TaskState state,
                       SkillProficiency targetProficiency, int estimatedHours, boolean satisfiedAtGeneration,
                       boolean ready, String initialPriority, int priorityPoints, String orderingReason,
                       List<Prerequisite> prerequisites) { }
    public record Prerequisite(UUID taskId, String skillName, boolean satisfiedAtGeneration, boolean satisfied) { }
    public record WeeklyFocus(UUID taskId, String title, int plannedHours) { }
}
