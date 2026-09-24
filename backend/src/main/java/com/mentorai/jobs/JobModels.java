package com.mentorai.jobs;

import com.mentorai.decision.dto.LearningDecision;
import com.mentorai.profile.dto.ProfileSkillResponse;
import com.mentorai.skills.entity.SkillProficiency;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class JobModels {
    private JobModels() { }
    public record ExtractRequest(@NotBlank @Size(max=20000) String description) { }
    public record Draft(String extractionVersion,String description,String title,String responsibilities,
                        String experience,String location,String technologies,List<String> requiredSkills,
                        List<String> preferredSkills,List<String> unclassifiedSkills,String limitation) { }
    public record AnalysisRequest(
            @NotBlank @Size(max=20000) String description,
            @NotNull @Size(max=300) String title,
            @NotNull @Size(max=10000) String responsibilities,
            @NotNull @Size(max=3000) String experience,
            @NotNull @Size(max=1000) String location,
            @NotNull @Size(max=3000) String technologies,
            @NotNull @Size(max=100) List<@NotBlank @Size(max=100) String> requiredSkills,
            @NotNull @Size(max=100) List<@NotBlank @Size(max=100) String> preferredSkills,
            @NotNull @Size(max=100) List<@NotBlank @Size(max=100) String> unclassifiedSkills,
            @NotNull @AssertTrue Boolean reviewed) { }
    public record SkillMatch(UUID skillId,String name,String requirement,SkillProficiency currentProficiency,
                             SkillProficiency comparisonTarget,String status,int weight) { }
    public record ProfileInputs(Instant updatedAt,Integer weeklyHours,List<ProfileSkillResponse> skills) { }
    public record Analysis(UUID id,Instant calculatedAt,String calculationVersion,Draft originalExtraction,
                           AnalysisRequest reviewedJob,ProfileInputs profileInputs,String status,Integer matchIndicator,
                           List<SkillMatch> skills,List<LearningDecision> priorities,String preparationVersion,
                           String methodology) { }
}
