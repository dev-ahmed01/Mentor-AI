package com.mentorai.career.dto;

import com.mentorai.career.entity.Career;
import com.mentorai.career.entity.EntryDifficulty;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CareerSummaryResponse(
        UUID id,
        String slug,
        String name,
        String description,
        EntryDifficulty entryDifficulty,
        int recommendedWeeklyHours,
        List<CareerSkillResponse> coreSkills,
        String dataLabel) {

    public static CareerSummaryResponse from(Career career) {
        return new CareerSummaryResponse(
                career.getId(),
                career.getSlug(),
                career.getName(),
                career.getDescription(),
                career.getEntryDifficulty(),
                career.getRecommendedWeeklyHours(),
                career.getSkills().stream()
                        .map(CareerSkillResponse::from)
                        .sorted(Comparator.comparingInt(CareerSkillResponse::importance).reversed()
                                .thenComparing(CareerSkillResponse::name))
                        .limit(6)
                        .toList(),
                career.isDemoData() ? "CONTROLLED CATALOG DATA" : "CATALOG DATA");
    }
}
