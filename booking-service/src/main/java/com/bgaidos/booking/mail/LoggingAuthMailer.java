package com.bgaidos.booking.mail;

import com.bgaidos.booking.util.MailTemplates;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class LoggingAuthMailer implements AuthMailer {

    @Override
    public void sendVerification(String email, String code, Duration expiresIn) {
        log.info("[DEV ONLY] email verification code for {} (expires in {}): {}",
            email, MailTemplates.formatDuration(expiresIn), code);
    }

    @Override
    public void sendPasswordReset(String email, String code, Duration expiresIn) {
        log.info("[DEV ONLY] password reset code for {} (expires in {}): {}",
            email, MailTemplates.formatDuration(expiresIn), code);
    }

    @Override
    public void sendBookingConfirmation(String email, UUID bookingId, BigDecimal total, String currency, List<String> camperNames) {
        log.info("[DEV ONLY] booking confirmation for {}: id={} total={} {} campers={}",
            email, bookingId, total, currency, camperNames);
    }
}
