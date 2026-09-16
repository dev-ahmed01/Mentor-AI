package com.mentorai.decision.controller;

import com.mentorai.decision.dto.LearningPrioritiesResponse;
import com.mentorai.decision.service.LearningDecisionService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decisions")
public class LearningDecisionController {
    private final LearningDecisionService decisions;
    public LearningDecisionController(LearningDecisionService decisions) { this.decisions = decisions; }

    @GetMapping("/learning-priorities")
    public LearningPrioritiesResponse priorities(@RequestParam UUID careerId, Authentication authentication) {
        return decisions.priorities(careerId, authentication);
    }
}
