package com.mentorai.skills.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SkillDependencyValidator implements ApplicationRunner {
    private final SkillDependencyService service;

    public SkillDependencyValidator(SkillDependencyService service) { this.service = service; }

    @Override
    public void run(ApplicationArguments arguments) { service.validateGraph(); }
}
