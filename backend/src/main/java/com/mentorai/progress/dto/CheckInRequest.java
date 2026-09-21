package com.mentorai.progress.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mentorai.progress.entity.ProgressEnums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record CheckInRequest(
        @NotNull UUID planId,
        @NotNull @Min(0) Long expectedRoadmapRevision,
        @NotNull @Min(0) @Max(168) @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer actualHours,
        @NotNull @Min(0) @Max(168) @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer availableHoursNextWeek,
        @Min(1) @Max(5) @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer difficultyRating,
        @Min(1) @Max(5) @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer confidenceRating,
        @NotNull EnergyBand energyOrCapacityBand,
        @NotNull @Size(max=5) List<@NotNull Blocker> blockers,
        @Size(max=500) String notes,
        @Valid Constraint constraint,
        @NotNull @Size(max=100) List<@NotNull @Valid TaskOutcome> tasks) {
    public record Constraint(@NotNull ConstraintType type,@NotNull LocalDate startDate,@NotNull LocalDate endDate) { }
    public record TaskOutcome(@NotNull UUID taskId,@NotNull Outcome outcome) { }
}
