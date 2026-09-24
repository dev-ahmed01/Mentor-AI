package com.mentorai.progress.service;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgressClockConfig {
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock progressClock() {
        return Clock.systemUTC();
    }
}
