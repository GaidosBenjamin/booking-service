package com.bgaidos.booking.mail;

import com.bgaidos.booking.util.MailTemplates;
import com.bgaidos.booking.util.MailTemplates.MailBody;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
public class BrevoAuthMailer implements AuthMailer {

    private final JavaMailSender mailSender;
    private final MailTemplates mailTemplates;
    private final String from;
    private final String brandName;

    @Override
    public void sendVerification(String email, String code, Duration expiresIn, Locale locale) {
        send(email, mailTemplates.verification(code, expiresIn, brandName, locale));
        log.debug("sent verification code to {}", email);
    }

    @Override
    public void sendPasswordReset(String email, String code, Duration expiresIn, Locale locale) {
        send(email, mailTemplates.passwordReset(code, expiresIn, brandName, locale));
        log.info("{} has been sent to {}", code, email);
    }

    @Override
    public void sendBookingConfirmation(String email, UUID bookingId, BigDecimal total, String currency, List<String> camperNames, Locale locale) {
        send(email, mailTemplates.bookingConfirmation(bookingId, total, currency, camperNames, brandName, locale));
        log.debug("sent booking confirmation to {}", email);
    }

    @Override
    public void sendDonationConfirmation(String email, UUID donationId, BigDecimal amount, String currency, String donorName, Locale locale) {
        send(email, mailTemplates.donationConfirmation(donationId, amount, currency, donorName, brandName, locale));
        log.debug("sent donation confirmation to {}", email);
    }

    @Override
    public void sendPaymentReminder(String email, List<String> camperNames, Locale locale) {
        send(email, mailTemplates.paymentReminder(camperNames, brandName, locale));
        log.debug("sent payment reminder to {}", email);
    }

    private void send(String to, MailBody body) {
        var message = mailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(body.subject());
            helper.setText(body.plainText(), body.html());
        } catch (MessagingException ex) {
            throw new MailParseException("failed to build auth email", ex);
        }
        mailSender.send(message);
    }
}
