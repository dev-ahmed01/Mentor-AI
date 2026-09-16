package com.mentorai.roadmap.dto;

import com.mentorai.roadmap.entity.TaskState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record UpdateRoadmapRequest(@NotNull @Min(0) Long expectedRevision,
                                   @Size(min = 1, max = 200) String title,
                                   @Size(max = 100) List<@NotNull @Valid TaskEdit> tasks) {
    public record TaskEdit(@NotNull UUID id, @NotBlank @Size(max = 200) String title,
                           @NotNull @Min(0) @Max(168) Integer estimatedHours, @NotNull TaskState state) { }
}
