package com.mentorai.simulator;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SkillSimulationRequest(@NotNull UUID skillId, @NotNull UUID targetCareerId) { }
