package com.mentorai.progress.adaptation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mentorai.progress.dto.StrictIntegerDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record AcceptAdaptationRequest(@NotNull @Min(0) Long expectedRoadmapRevision,
        @NotNull @Min(0) Long expectedPlanRevision, @Valid Edit edit) {
    public record Edit(@NotNull @Min(0) @Max(168) @JsonDeserialize(using=StrictIntegerDeserializer.class) Integer capacityHours,
                       @NotNull @Size(max=20) List<@NotNull @Valid Task> tasks) { }
    public record Task(@NotNull UUID taskId,
                       @NotNull @Min(1) @Max(168) @JsonDeserialize(using=StrictIntegerDeserializer.class) Integer plannedHours) { }
}
