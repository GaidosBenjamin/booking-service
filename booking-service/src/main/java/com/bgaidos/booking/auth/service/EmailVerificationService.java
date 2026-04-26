package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.auth.service.event.VerificationCodeIssuedEvent;
import com.bgaidos.booking.util.AuthTokens;
import com.bgaidos.booking.entity.EmailVerificationToken;
import com.bgaidos.booking.entity.User;
import com.bgaidos.booking.repo.EmailVerificationTokenRepository;
import com.bgaidos.booking.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final TenantLookup tenantLookup;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.auth.verification.ttl}")
    private Duration ttl;

    public void issue(User user) {
        var now = Instant.now();
        tokenRepository.invalidateAllForUser(user.getId(), now);

        var code = AuthTokens.randomCode();

        var token = new EmailVerificationToken();
        token.setTenantId(user.getTenantId());
        token.setUser(user);
        token.setTokenHash(AuthTokens.hash(code));
        token.setExpiresAt(now.plus(ttl));
        tokenRepository.save(token);

        eventPublisher.publishEvent(new VerificationCodeIssuedEvent(user.getEmail(), code, ttl));
        log.info("issued email verification code for user={}", user.getId());
    }

    public void resend(String organizationSlug, String email) {
        var organization = tenantLookup.findOrganizationBySlug(organizationSlug).orElse(null);
        if (organization == null) {
            log.info("resend verification for unknown org slug={}", organizationSlug);
            return;
        }
        var user = tenantLookup.findUserByEmail(organization.getId(), email).orElse(null);
        if (user == null) {
            log.info("resend verification for unknown email tenant={} email={}", organization.getId(), email);
            return;
        }
        if (user.isEmailVerified()) {
            log.info("resend verification skipped for already-verified user={}", user.getId());
            return;
        }

        issue(user);
    }

    public void verify(String organizationSlug, String email, String code) {
        var organization = tenantLookup.findOrganizationBySlug(organizationSlug)
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        var user = tenantLookup.findUserByEmail(organization.getId(), email)
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        var token = tokenRepository
            .findActiveByUserIdAndTokenHash(user.getId(), AuthTokens.hash(code))
            .orElseThrow(() -> new BadRequestException("invalid or expired code"));

        assertNotExpired(token);
        var now = Instant.now();
        user.setEmailVerified(true);
        token.setConsumedAt(now);
        log.info("verified email for user={}", user.getId());
    }

    private static void assertNotExpired(EmailVerificationToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("invalid or expired code");
        }
    }
}
