package com.mentorai.career.dto;

import com.mentorai.career.entity.SkillRequirement;
import com.mentorai.skills.entity.SkillProficiency;
import java.util.UUID;

public record SkillGapResponse(
        UUID skillId,
        String name,
        String category,
        int importance,
        SkillRequirement requirement,
        SkillProficiency currentProficiency,
        String priority) {
}
