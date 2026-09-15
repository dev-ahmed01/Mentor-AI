package com.mentorai.skills.controller;

import com.mentorai.skills.dto.SkillDependencyResponse;
import com.mentorai.skills.dto.SkillPrerequisitesResponse;
import com.mentorai.skills.service.SkillDependencyService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillDependencyService service;
    public SkillController(SkillDependencyService service) { this.service = service; }

    @GetMapping("/{id}/dependencies")
    List<SkillDependencyResponse> dependencies(@PathVariable UUID id) { return service.dependencies(id); }

    @GetMapping("/{id}/prerequisites")
    SkillPrerequisitesResponse prerequisites(@PathVariable UUID id, Authentication authentication) {
        return service.prerequisites(id, authentication);
    }

    @GetMapping("/prerequisites")
    List<SkillPrerequisitesResponse> forCareer(@RequestParam UUID careerId, Authentication authentication) {
        return service.forCareer(careerId, authentication);
    }
}
