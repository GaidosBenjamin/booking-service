package com.bgaidos.booking.mail;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface AuthMailer {

    void sendVerification(String email, String code, Duration expiresIn);

    void sendPasswordReset(String email, String code, Duration expiresIn);

    void sendBookingConfirmation(String email, UUID bookingId, BigDecimal total, String currency, List<String> camperNames);
}
