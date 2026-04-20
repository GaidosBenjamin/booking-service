package com.bgaidos.booking.auth.mail;

import java.time.Duration;

public interface AuthMailer {

    void sendVerification(String email, String code, Duration expiresIn);

    void sendPasswordReset(String email, String code, Duration expiresIn);
}
