package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.auth.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.auth.jwt.ttl}")
    private Duration ttl;

    public IssuedToken issue(AuthUser authUser) {
        var now = Instant.now();
        var expiresAt = now.plus(ttl);

        var authorities = authUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        var claims = JwtClaimsSet.builder()
            .subject(authUser.getUserId().toString())
            .claim("tid", authUser.getTenantId().toString())
            .claim("email", authUser.getUsername())
            .claim("authorities", authorities)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build();

        var token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        return new IssuedToken(token, ttl.toSeconds());
    }

    public record IssuedToken(String value, long expiresInSeconds) {
    }
}
