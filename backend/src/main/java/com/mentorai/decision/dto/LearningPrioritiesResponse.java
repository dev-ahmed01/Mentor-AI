package com.mentorai.decision.dto;

import java.util.List;
import java.util.UUID;

public record LearningPrioritiesResponse(
        UUID careerId, String careerName, String calculationVersion, String dataLabel,
        Integer weeklyHours, int immediateFocusLimit, String marketEvidenceStatus,
        int marketWeight, List<LearningDecision> decisions) { }
