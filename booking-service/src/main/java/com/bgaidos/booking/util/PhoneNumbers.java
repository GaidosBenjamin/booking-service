package com.bgaidos.booking.util;

import java.util.Optional;

public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    public static Optional<String> toE164(String stored, String defaultCountryCode) {
        if (stored == null || stored.isBlank()) return Optional.empty();
        var cleaned = stored.strip();
        if (cleaned.startsWith("+")) {
            if (cleaned.matches("\\+\\d{10,15}")) return Optional.of(cleaned);
            return Optional.empty();
        }
        if (cleaned.matches("0\\d{9}")) {
            return Optional.of(defaultCountryCode + cleaned.substring(1));
        }
        return Optional.empty();
    }
}
