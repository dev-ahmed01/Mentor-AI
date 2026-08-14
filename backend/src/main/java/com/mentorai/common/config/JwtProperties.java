package com.mentorai.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mentorai.security.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
