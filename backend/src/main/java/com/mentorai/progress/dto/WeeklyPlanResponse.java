package com.mentorai.progress.dto;
import java.time.Instant; import java.time.LocalDate; import java.util.List; import java.util.UUID;
public record WeeklyPlanResponse(UUID id,UUID roadmapId,String roadmapTitle,LocalDate weekStart,int capacityHours,
        int plannedHours,long roadmapRevision,List<WeeklyTask> tasks,String reason,Instant createdAt,long revision,String mode) {
    public record WeeklyTask(UUID taskId,String title,int plannedHours) { }
}
