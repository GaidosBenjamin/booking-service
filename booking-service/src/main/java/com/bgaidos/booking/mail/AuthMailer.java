package com.bgaidos.booking.mail;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface AuthMailer {

    void sendVerification(String email, String code, Duration expiresIn, Locale locale);

    void sendPasswordReset(String email, String code, Duration expiresIn, Locale locale);

    void sendBookingConfirmation(String email, UUID bookingId, BigDecimal total, String currency, List<String> camperNames, Locale locale);

    void sendDonationConfirmation(String email, UUID donationId, BigDecimal amount, String currency, String donorName, Locale locale);

    void sendPaymentReminder(String email, List<String> camperNames, Locale locale);
}
