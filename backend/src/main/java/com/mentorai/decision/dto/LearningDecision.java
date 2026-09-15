package com.mentorai.decision.dto;

import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.entity.SkillProficiency;
import java.util.List;
import java.util.UUID;

public record LearningDecision(
        UUID skillId, String name, LearningPriority priority, int deterministicScore,
        SkillPrerequisitesResponse prerequisiteReadiness, String careerRelevance,
        int importance, SkillProficiency currentProficiency, SkillProficiency targetProficiency,
        int learningDistance, String estimatedEffortBand, int bottleneckCount,
        List<String> reasonCodes, String evidenceStatus) { }
