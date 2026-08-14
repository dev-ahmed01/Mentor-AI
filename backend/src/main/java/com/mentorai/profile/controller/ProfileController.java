package com.mentorai.profile.controller;

import com.mentorai.profile.dto.ProfileResponse;
import com.mentorai.profile.dto.UpdateProfileRequest;
import com.mentorai.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    ProfileResponse get(Authentication authentication) {
        return profileService.get(authentication);
    }

    @PutMapping
    ProfileResponse update(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(authentication, request);
    }
}
