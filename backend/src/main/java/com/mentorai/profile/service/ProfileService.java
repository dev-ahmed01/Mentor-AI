package com.mentorai.profile.service;

import com.mentorai.auth.entity.User;
import com.mentorai.auth.service.AuthService;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.profile.dto.ProfileResponse;
import com.mentorai.profile.dto.ProfileSkillRequest;
import com.mentorai.profile.dto.UpdateProfileRequest;
import com.mentorai.profile.entity.StudentProfile;
import com.mentorai.profile.repository.StudentProfileRepository;
import com.mentorai.skills.entity.Skill;
import com.mentorai.skills.entity.StudentSkill;
import com.mentorai.skills.repository.SkillRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final AuthService authService;
    private final StudentProfileRepository profileRepository;
    private final SkillRepository skillRepository;

    public ProfileService(
            AuthService authService,
            StudentProfileRepository profileRepository,
            SkillRepository skillRepository) {
        this.authService = authService;
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(Authentication authentication) {
        User user = authService.requireUser(authentication);
        return ProfileResponse.from(requireProfile(user));
    }

    @Transactional
    public ProfileResponse update(Authentication authentication, UpdateProfileRequest request) {
        User user = authService.requireUser(authentication);
        StudentProfile profile = requireProfile(user);
        profile.setDegree(clean(request.degree()));
        profile.setYear(request.year());
        profile.setSemester(request.semester());
        profile.setRemotePreference(request.remotePreference());
        profile.setTimeAvailablePerWeek(request.timeAvailablePerWeek());
        profile.setExperience(clean(request.experience()));
        profile.setShortTermGoal(clean(request.shortTermGoal()));
        profile.setLongTermGoal(clean(request.longTermGoal()));
        replace(profile.getInterests(), request.interests());
        replace(profile.getGoals(), request.goals());
        replace(profile.getProgrammingLanguages(), request.programmingLanguages());
        replace(profile.getPreferredDomains(), request.preferredDomains());
        replace(profile.getTargetLocations(), request.targetLocations());
        replace(profile.getCurrentProjects(), request.currentProjects());
        replace(profile.getCertifications(), request.certifications());
        replace(profile.getAvoidances(), request.avoidances());
        profile.replaceSkills(toStudentSkills(profile, request.skills()));
        return ProfileResponse.from(profileRepository.save(profile));
    }

    private StudentProfile requireProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile was not found."));
    }

    private List<StudentSkill> toStudentSkills(
            StudentProfile profile, List<ProfileSkillRequest> requests) {
        Map<String, ProfileSkillRequest> unique = new LinkedHashMap<>();
        for (ProfileSkillRequest request : safe(requests)) {
            unique.putIfAbsent(normalizeSkillName(request.name()), request);
        }
        List<StudentSkill> results = new ArrayList<>();
        unique.forEach((normalizedName, request) -> {
            Skill skill = skillRepository.findByNormalizedName(normalizedName)
                    .orElseGet(() -> skillRepository.save(new Skill(
                            request.name().strip(), normalizedName, request.category().strip())));
            results.add(new StudentSkill(
                    profile, skill, request.proficiency(), request.confidence(), request.source()));
        });
        return results;
    }

    private String normalizeSkillName(String value) {
        return value.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private void replace(List<String> target, List<String> source) {
        target.clear();
        Map<String, String> unique = new LinkedHashMap<>();
        safe(source).stream()
                .map(this::clean)
                .filter(value -> value != null && !value.isBlank())
                .forEach(value -> unique.putIfAbsent(value.toLowerCase(Locale.ROOT), value));
        target.addAll(unique.values());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.strip();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
