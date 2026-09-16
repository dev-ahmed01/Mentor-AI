package com.mentorai.career.dto;

import com.mentorai.career.entity.CareerSkill;
import com.mentorai.career.entity.SkillRequirement;
import java.util.UUID;

public record CareerSkillResponse(
        UUID id,
        String name,
        String category,
        int importance,
        SkillRequirement requirement) {

    public static CareerSkillResponse from(CareerSkill careerSkill) {
        return new CareerSkillResponse(
                careerSkill.getSkill().getId(),
                careerSkill.getSkill().getName(),
                careerSkill.getSkill().getCategory(),
                careerSkill.getImportance(),
                careerSkill.getRequirement());
    }
}
