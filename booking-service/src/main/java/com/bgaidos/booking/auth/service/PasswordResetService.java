package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.ForgotPasswordRequest;
import com.bgaidos.booking.api.auth.ResetPasswordRequest;
import com.bgaidos.booking.auth.service.event.PasswordResetCodeIssuedEvent;
import com.bgaidos.booking.auth.util.AuthTokens;
import com.bgaidos.booking.data.entity.PasswordResetToken;
import com.bgaidos.booking.data.repo.PasswordResetTokenRepository;
import com.bgaidos.booking.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {

    private final TenantLookup tenantLookup;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.auth.reset.ttl}")
    private Duration ttl;

    public void requestReset(ForgotPasswordRequest request) {
        var organization = tenantLookup.findOrganizationBySlug(request.organizationSlug()).orElse(null);
        if (organization == null) {
            log.info("forgot-password for unknown org slug={}", request.organizationSlug());
            return;
        }
        var user = tenantLookup.findUserByEmail(organization.getId(), request.email()).orElse(null);
        if (user == null) {
            log.info("forgot-password for unknown email tenant={} email={}", organization.getId(), request.email());
            return;
        }

        var now = Instant.now();
        tokenRepository.invalidateAllForUser(user.getId(), now);

        var code = AuthTokens.randomCode();

        var token = new PasswordResetToken();
        token.setTenantId(organization.getId());
        token.setUser(user);
        token.setTokenHash(AuthTokens.hash(code));
        token.setExpiresAt(now.plus(ttl));
        tokenRepository.save(token);

        eventPublisher.publishEvent(new PasswordResetCodeIssuedEvent(user.getEmail(), code, ttl));
    }

    public void resetPassword(ResetPasswordRequest request) {
        var organization = tenantLookup.findOrganizationBySlug(request.organizationSlug())
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        var user = tenantLookup.findUserByEmail(organization.getId(), request.email())
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        var token = tokenRepository
            .findActiveByUserIdAndTokenHash(user.getId(), AuthTokens.hash(request.code()))
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        var now = Instant.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("invalid or expired code");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setConsumedAt(now);

        refreshTokenService.revokeAllForUser(user.getId());
    }
}
