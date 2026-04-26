package com.bgaidos.booking.mail;

import com.bgaidos.booking.auth.service.event.BookingConfirmedEvent;
import com.bgaidos.booking.auth.service.event.PasswordResetCodeIssuedEvent;
import com.bgaidos.booking.auth.service.event.VerificationCodeIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMailEventListener {

    private final AuthMailer mailer;

    @Async("authMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationIssued(VerificationCodeIssuedEvent event) {
        try {
            mailer.sendVerification(event.email(), event.code(), event.expiresIn());
        } catch (RuntimeException ex) {
            log.warn("failed to send verification email to {}", event.email(), ex);
        }
    }

    @Async("authMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetIssued(PasswordResetCodeIssuedEvent event) {
        try {
            mailer.sendPasswordReset(event.email(), event.code(), event.expiresIn());
        } catch (RuntimeException ex) {
            log.warn("failed to send password reset email to {}", event.email(), ex);
        }
    }

    @Async("authMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        try {
            mailer.sendBookingConfirmation(event.email(), event.bookingId(), event.total(), event.currency(), event.camperNames());
        } catch (RuntimeException ex) {
            log.warn("failed to send booking confirmation to {}", event.email(), ex);
        }
    }
}
