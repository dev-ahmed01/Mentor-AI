package com.mentorai.skills.dto;

import java.util.UUID;

public record SkillDependencyResponse(
        UUID skillId, UUID prerequisiteSkillId, String prerequisiteName, int importance, String dataLabel) { }
