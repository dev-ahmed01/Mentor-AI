package com.mentorai.roadmap.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRoadmapRequest(@NotNull UUID careerId) { }
