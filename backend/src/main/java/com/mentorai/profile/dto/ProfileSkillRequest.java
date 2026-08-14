package com.mentorai.profile.dto;

import com.mentorai.skills.entity.SkillConfidence;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.entity.SkillSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileSkillRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 60) String category,
        @NotNull SkillProficiency proficiency,
        @NotNull SkillConfidence confidence,
        @NotNull SkillSource source) {
}
