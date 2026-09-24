package com.mentorai.career.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CareerAnalysisResponse(
        Instant calculatedAt,
        String calculationVersion,
        String scoreName,
        String scoreMode,
        String methodology,
        Map<String, Integer> configuredWeights,
        int availableEvidenceWeight,
        String marketEvidenceStatus,
        List<String> profileLimitations,
        List<CareerCandidateResponse> candidates) {
}
