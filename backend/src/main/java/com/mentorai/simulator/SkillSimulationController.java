package com.mentorai.simulator;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/simulator")
public class SkillSimulationController {
    private final SkillSimulationService service;
    public SkillSimulationController(SkillSimulationService service) { this.service=service; }
    @PostMapping("/skill")
    public SkillSimulationResponse simulate(Authentication authentication,@Valid @RequestBody SkillSimulationRequest request) {
        return service.simulate(authentication,request);
    }
}
