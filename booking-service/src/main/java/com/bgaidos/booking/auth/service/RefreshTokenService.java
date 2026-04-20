package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.auth.util.AuthTokens;
import com.bgaidos.booking.data.entity.RefreshToken;
import com.bgaidos.booking.data.entity.User;
import com.bgaidos.booking.data.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;

    @Value("${app.auth.refresh.ttl}")
    private Duration ttl;

    public IssuedRefresh issueForNewSession(User user) {
        return insert(user, UUID.randomUUID(), Instant.now());
    }

    public Rotated rotate(String rawPresented) {
        var now = Instant.now();
        var existing = tokenRepository.findByTokenHash(AuthTokens.hash(rawPresented))
            .orElseThrow(() -> new BadCredentialsException("invalid credentials"));

        if (existing.getRevokedAt() != null) {
            tokenRepository.revokeFamily(existing.getFamilyId(), now);
            throw new BadCredentialsException("invalid credentials");
        }
        if (existing.getExpiresAt().isBefore(now)) {
            throw new BadCredentialsException("invalid credentials");
        }

        existing.setRevokedAt(now);

        var user = existing.getUser();
        var issued = insert(user, existing.getFamilyId(), now);
        return new Rotated(issued, user);
    }

    public void revoke(String rawPresented) {
        tokenRepository.findByTokenHash(AuthTokens.hash(rawPresented))
            .filter(t -> t.getRevokedAt() == null)
            .ifPresent(t -> t.setRevokedAt(Instant.now()));
    }

    public void revokeAllForUser(UUID userId) {
        tokenRepository.revokeAllForUser(userId, Instant.now());
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
