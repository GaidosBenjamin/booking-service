package com.bgaidos.booking.auth.mail;

import com.bgaidos.booking.auth.util.AuthMailTemplates;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class LoggingAuthMailer implements AuthMailer {

    @Override
    public void sendVerification(String email, String code, Duration expiresIn) {
        log.info("[DEV ONLY] email verification code for {} (expires in {}): {}",
            email, AuthMailTemplates.formatDuration(expiresIn), code);
    }

    @Override
    public void sendPasswordReset(String email, String code, Duration expiresIn) {
        log.info("[DEV ONLY] password reset code for {} (expires in {}): {}",
            email, AuthMailTemplates.formatDuration(expiresIn), code);
    }
}
