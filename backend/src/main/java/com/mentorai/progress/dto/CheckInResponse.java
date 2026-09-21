package com.mentorai.progress.dto;
import com.mentorai.progress.entity.ProgressEnums.*; import java.time.Instant; import java.time.LocalDate; import java.util.List; import java.util.UUID;
public record CheckInResponse(UUID id,UUID planId,UUID roadmapId,LocalDate weekStart,int plannedHours,int actualHours,
        int availableHoursNextWeek,Integer difficultyRating,Integer confidenceRating,EnergyBand energyOrCapacityBand,
        List<Blocker> blockers,String notes,Constraint constraint,List<Task> tasks,WeeklyPlanResponse nextPlan,
        String explanation,Instant createdAt) {
    public record Constraint(ConstraintType type,LocalDate startDate,LocalDate endDate) { }
    public record Task(UUID taskId,String title,Outcome outcome) { }
}
