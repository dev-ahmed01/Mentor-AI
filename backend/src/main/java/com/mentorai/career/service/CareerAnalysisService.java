package com.mentorai.career.service;

import com.mentorai.auth.entity.User;
import com.mentorai.auth.service.AuthService;
import com.mentorai.career.dto.CareerAnalysisResponse;
import com.mentorai.career.entity.Career;
import com.mentorai.career.repository.CareerRepository;
import com.mentorai.common.exception.ProfileIncompleteException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.profile.entity.StudentProfile;
import com.mentorai.profile.repository.StudentProfileRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareerAnalysisService {

    private final AuthService authService;
    private final StudentProfileRepository profileRepository;
    private final CareerRepository careerRepository;
    private final CareerFitScoringService scoringService;

    public CareerAnalysisService(
            AuthService authService,
            StudentProfileRepository profileRepository,
            CareerRepository careerRepository,
            CareerFitScoringService scoringService) {
        this.authService = authService;
        this.profileRepository = profileRepository;
        this.careerRepository = careerRepository;
        this.scoringService = scoringService;
    }

    @Transactional(readOnly = true)
    public CareerAnalysisResponse analyze(Authentication authentication, int limit) {
        User user = authService.requireUser(authentication);
        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile was not found."));
        if (profile.getInterests().isEmpty()
                && profile.getPreferredDomains().isEmpty()
                && profile.getGoals().isEmpty()
                && profile.getSkills().isEmpty()) {
            throw new ProfileIncompleteException(
                    "Add at least one interest, preferred domain, goal, or skill before analyzing careers.");
        }
        List<Career> careers = careerRepository.findAllByActiveTrueOrderByNameAsc();
        return scoringService.score(profile, careers, limit);
    }
}
