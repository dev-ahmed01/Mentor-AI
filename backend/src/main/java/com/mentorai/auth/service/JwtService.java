package com.mentorai.auth.service;

import com.mentorai.auth.entity.User;
import com.mentorai.common.config.JwtProperties;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration expiration;

    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.expiration = properties.expiration() == null ? Duration.ofHours(8) : properties.expiration();
    }

    public IssuedToken issue(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mentorai")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("uid", user.getId().toString())
                .claim("roles", new String[] {user.getRole().name()})
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
