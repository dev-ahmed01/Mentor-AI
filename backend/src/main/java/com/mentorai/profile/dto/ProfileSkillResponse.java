package com.mentorai.profile.dto;

import com.mentorai.skills.entity.StudentSkill;
import com.mentorai.skills.entity.SkillConfidence;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.entity.SkillSource;
import java.util.UUID;

public record ProfileSkillResponse(
        UUID id,
        String name,
        String category,
        SkillProficiency proficiency,
        SkillConfidence confidence,
        SkillSource source) {

    public static ProfileSkillResponse from(StudentSkill studentSkill) {
        return new ProfileSkillResponse(
                studentSkill.getSkill().getId(),
                studentSkill.getSkill().getName(),
                studentSkill.getSkill().getCategory(),
                studentSkill.getProficiency(),
                studentSkill.getConfidence(),
                studentSkill.getSource());
    }
}
