package com.mentorai.skills.dto;

import com.mentorai.skills.entity.SkillProficiency;
import java.util.List;
import java.util.UUID;

public record SkillPrerequisitesResponse(
        UUID skillId, String name, boolean eligible, String coverage,
        SkillProficiency minimumProficiency, String calculationVersion, String dataLabel,
        List<Prerequisite> prerequisites) {
    public record Prerequisite(UUID skillId, String name, boolean direct,
                               SkillProficiency currentProficiency, boolean satisfied) { }
}
