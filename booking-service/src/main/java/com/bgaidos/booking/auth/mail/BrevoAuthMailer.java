package com.bgaidos.booking.auth.mail;

import com.bgaidos.booking.auth.util.AuthMailTemplates;
import com.bgaidos.booking.auth.util.AuthMailTemplates.MailBody;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class BrevoAuthMailer implements AuthMailer {

    private final JavaMailSender mailSender;
    private final String from;
    private final String brandName;

    @Override
    public void sendVerification(String email, String code, Duration expiresIn) {
        send(email, AuthMailTemplates.verification(code, expiresIn, brandName));
        log.debug("sent verification code to {}", email);
    }

    @Override
    public void sendPasswordReset(String email, String code, Duration expiresIn) {
        send(email, AuthMailTemplates.passwordReset(code, expiresIn, brandName));
        log.debug("sent password reset code to {}", email);
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
