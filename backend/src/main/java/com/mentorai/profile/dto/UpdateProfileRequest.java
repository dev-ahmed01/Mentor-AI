package com.mentorai.profile.dto;

import com.mentorai.profile.entity.RemotePreference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateProfileRequest(
        @Size(max = 120) String degree,
        @Min(1) @Max(8) Integer year,
        @Min(1) @Max(16) Integer semester,
        @Size(max = 30) List<@Size(max = 120) String> interests,
        @Size(max = 30) List<@Size(max = 300) String> goals,
        @Size(max = 30) List<@Size(max = 100) String> programmingLanguages,
        @Size(max = 30) List<@Size(max = 120) String> preferredDomains,
        @Size(max = 30) List<@Size(max = 120) String> targetLocations,
        RemotePreference remotePreference,
        @Min(1) @Max(168) Integer timeAvailablePerWeek,
        @Size(max = 50) List<@Valid ProfileSkillRequest> skills,
        @Size(max = 30) List<@Size(max = 300) String> currentProjects,
        @Size(max = 2000) String experience,
        @Size(max = 30) List<@Size(max = 200) String> certifications,
        @Size(max = 1000) String shortTermGoal,
        @Size(max = 1000) String longTermGoal,
        @Size(max = 30) List<@Size(max = 200) String> avoidances) {
}
