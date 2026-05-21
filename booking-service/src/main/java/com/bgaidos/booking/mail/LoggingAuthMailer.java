package com.bgaidos.booking.mail;

import com.bgaidos.booking.util.MailTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LoggingAuthMailer implements AuthMailer {

    private final MailTemplates mailTemplates;

    @Override
    public void sendVerification(String email, String code, Duration expiresIn, Locale locale) {
        log.info("[DEV ONLY] email verification code for {} (expires in {}, lang={}): {}",
            email, mailTemplates.formatDuration(expiresIn, locale), locale.getLanguage(), code);
    }

    @Override
    public void sendPasswordReset(String email, String code, Duration expiresIn, Locale locale) {
        log.info("[DEV ONLY] password reset code for {} (expires in {}, lang={}): {}",
            email, mailTemplates.formatDuration(expiresIn, locale), locale.getLanguage(), code);
    }

    @Override
    public void sendBookingConfirmation(String email, UUID bookingId, BigDecimal total, String currency, List<String> camperNames, Locale locale) {
        log.info("[DEV ONLY] booking confirmation for {} (lang={}): id={} total={} {} campers={}",
            email, locale.getLanguage(), bookingId, total, currency, camperNames);
    }

    @Override
    public void sendDonationConfirmation(String email, UUID donationId, BigDecimal amount, String currency, String donorName, Locale locale) {
        log.info("[DEV ONLY] donation confirmation for {} (lang={}): id={} amount={} {} donor={}",
            email, locale.getLanguage(), donationId, amount, currency, donorName);
    }
}
