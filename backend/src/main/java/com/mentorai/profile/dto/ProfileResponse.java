package com.mentorai.profile.dto;

import com.mentorai.profile.entity.RemotePreference;
import com.mentorai.profile.entity.StudentProfile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String degree,
        Integer year,
        Integer semester,
        List<String> interests,
        List<String> goals,
        List<String> programmingLanguages,
        List<String> preferredDomains,
        List<String> targetLocations,
        RemotePreference remotePreference,
        Integer timeAvailablePerWeek,
        List<ProfileSkillResponse> skills,
        List<String> currentProjects,
        String experience,
        List<String> certifications,
        String shortTermGoal,
        String longTermGoal,
        List<String> avoidances,
        Instant updatedAt) {

    public static ProfileResponse from(StudentProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getDegree(),
                profile.getYear(),
                profile.getSemester(),
                List.copyOf(profile.getInterests()),
                List.copyOf(profile.getGoals()),
                List.copyOf(profile.getProgrammingLanguages()),
                List.copyOf(profile.getPreferredDomains()),
                List.copyOf(profile.getTargetLocations()),
                profile.getRemotePreference(),
                profile.getTimeAvailablePerWeek(),
                profile.getSkills().stream().map(ProfileSkillResponse::from).toList(),
                List.copyOf(profile.getCurrentProjects()),
                profile.getExperience(),
                List.copyOf(profile.getCertifications()),
                profile.getShortTermGoal(),
                profile.getLongTermGoal(),
                List.copyOf(profile.getAvoidances()),
                profile.getUpdatedAt());
    }
}
