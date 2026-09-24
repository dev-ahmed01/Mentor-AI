package com.mentorai.demo;

import com.mentorai.progress.dto.CheckInResponse;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.UUID;

public final class DemoModels {
    private DemoModels() { }
    public record StartRequest(@NotNull @AssertTrue Boolean confirmSynthetic) { }
    public record ExamRequest(@NotNull @Min(0) Long expectedRoadmapRevision,@NotNull @Min(0) Long expectedPlanRevision) { }
    public record Run(UUID id,UUID roadmapId,UUID planId,LocalDate weekStart,long roadmapRevision,long planRevision,
                      String scenarioVersion,Instant createdAt,UUID examCheckInId) { }
    public record Status(boolean enabled,Run run,CheckInResponse checkIn) { }
}
