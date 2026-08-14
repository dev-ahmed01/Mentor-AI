package com.mentorai.auth.service;

import com.mentorai.auth.dto.AuthResponse;
import com.mentorai.auth.dto.LoginRequest;
import com.mentorai.auth.dto.RegisterRequest;
import com.mentorai.auth.dto.UserResponse;
import com.mentorai.auth.entity.User;
import com.mentorai.auth.repository.UserRepository;
import com.mentorai.common.exception.ConflictException;
import com.mentorai.common.exception.ResourceNotFoundException;
import com.mentorai.profile.entity.StudentProfile;
import com.mentorai.profile.repository.StudentProfileRepository;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            StudentProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists.");
        }
        User user = userRepository.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().strip()));
        profileRepository.save(new StudentProfile(user));
        return responseFor(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        return responseFor((User) authentication.getPrincipal());
    }

    @Transactional(readOnly = true)
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(requireUser(authentication));
    }

    public User requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authenticated user was not found.");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));
    }

    private AuthResponse responseFor(User user) {
        JwtService.IssuedToken issued = jwtService.issue(user);
        return new AuthResponse(issued.value(), "Bearer", issued.expiresAt(), UserResponse.from(user));
    }
}
