package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.util.AuthTokens;
import com.bgaidos.booking.entity.RefreshToken;
import com.bgaidos.booking.entity.User;
import com.bgaidos.booking.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;

    @Value("${app.auth.refresh.ttl}")
    private Duration ttl;

    public IssuedRefresh issueForNewSession(User user) {
        var issued = insert(user, UUID.randomUUID(), Instant.now());
        log.info("issued refresh token for user={} family={}", user.getId(), issued.familyId());
        return issued;
    }

    public Rotated rotate(String rawPresented) {
        var now = Instant.now();
        var existing = tokenRepository.findByTokenHash(AuthTokens.hash(rawPresented))
            .orElseThrow(() -> new BadCredentialsException("invalid credentials"));

        if (existing.getRevokedAt() != null) {
            log.warn("refresh token reuse detected — revoking family={} user={}",
                existing.getFamilyId(), existing.getUser().getId());
            tokenRepository.revokeFamily(existing.getFamilyId(), now);
            throw new BadCredentialsException("invalid credentials");
        }
        if (existing.getExpiresAt().isBefore(now)) {
            throw new BadCredentialsException("invalid credentials");
        }

        existing.setRevokedAt(now);

        var user = existing.getUser();
        var issued = insert(user, existing.getFamilyId(), now);
        log.info("rotated refresh token user={} family={}", user.getId(), existing.getFamilyId());
        return new Rotated(issued, user);
    }

    public void revoke(String rawPresented) {
        tokenRepository.findByTokenHash(AuthTokens.hash(rawPresented))
            .filter(t -> t.getRevokedAt() == null)
            .ifPresent(t -> {
                t.setRevokedAt(Instant.now());
                log.info("revoked refresh token user={}", t.getUser().getId());
            });
    }

    public void revokeAllForUser(UUID userId) {
        tokenRepository.revokeAllForUser(userId, Instant.now());
        log.info("revoked all active refresh tokens for user={}", userId);
    }

    private IssuedRefresh insert(User user, UUID familyId, Instant now) {
        var raw = AuthTokens.randomOpaqueToken();
        var token = new RefreshToken();
        token.setTenantId(user.getTenantId());
        token.setUser(user);
        token.setFamilyId(familyId);
        token.setTokenHash(AuthTokens.hash(raw));
        token.setExpiresAt(now.plus(ttl));
        tokenRepository.save(token);
        return new IssuedRefresh(raw, familyId, ttl.toSeconds());
    }

    public record IssuedRefresh(String value, UUID familyId, long expiresInSeconds) {
    }

    public record Rotated(IssuedRefresh refresh, User user) {
    }
}
