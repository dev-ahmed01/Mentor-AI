package com.mentorai.progress.dto;
import jakarta.validation.constraints.NotNull; import java.util.UUID;
public record CreateWeeklyPlanRequest(@NotNull UUID roadmapId) { }
