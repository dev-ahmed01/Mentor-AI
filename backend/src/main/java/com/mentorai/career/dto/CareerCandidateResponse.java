package com.mentorai.career.dto;

import java.util.List;
import java.util.UUID;

public record CareerCandidateResponse(
        UUID careerId,
        String slug,
        String careerName,
        int careerFitIndicator,
        FitBand fitBand,
        CareerFitFactors factors,
        int evidenceCoverage,
        List<String> reasons,
        List<String> strengths,
        List<SkillGapResponse> skillGaps,
        List<String> risks,
        List<AlternativeCareerResponse> alternatives,
        List<String> uncertainties,
        String nextStep) {
}
