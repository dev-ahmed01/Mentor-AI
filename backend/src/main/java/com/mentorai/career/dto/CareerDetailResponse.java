package com.mentorai.career.dto;

import com.mentorai.career.entity.Career;
import com.mentorai.career.entity.EntryDifficulty;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CareerDetailResponse(
        UUID id,
        String slug,
        String name,
        String description,
        EntryDifficulty entryDifficulty,
        String degreeRelevance,
        String projectExpectations,
        String internshipExpectations,
        String commonMisconceptions,
        String realitySummary,
        int recommendedWeeklyHours,
        List<String> responsibilities,
        List<String> commonJobTitles,
        List<CareerSkillResponse> skills,
        List<String> risks,
        List<String> marketConsiderations,
        String marketEvidenceStatus,
        String dataLabel,
        Instant updatedAt) {

    public static CareerDetailResponse from(Career career) {
        return new CareerDetailResponse(
                career.getId(),
                career.getSlug(),
                career.getName(),
                career.getDescription(),
                career.getEntryDifficulty(),
                career.getDegreeRelevance(),
                career.getProjectExpectations(),
                career.getInternshipExpectations(),
                career.getCommonMisconceptions(),
                career.getRealitySummary(),
                career.getRecommendedWeeklyHours(),
                career.getResponsibilities().stream().sorted().toList(),
                career.getCommonJobTitles().stream().sorted().toList(),
                career.getSkills().stream()
                        .map(CareerSkillResponse::from)
                        .sorted(Comparator.comparingInt(CareerSkillResponse::importance).reversed()
                                .thenComparing(CareerSkillResponse::name))
                        .toList(),
                career.getRisks().stream().sorted().toList(),
                career.getMarketConsiderations().stream().sorted().toList(),
                "INSUFFICIENT_MARKET_EVIDENCE",
                career.isDemoData() ? "CONTROLLED CATALOG DATA — NOT LIVE MARKET EVIDENCE" : "CATALOG DATA",
                career.getUpdatedAt());
    }
}
