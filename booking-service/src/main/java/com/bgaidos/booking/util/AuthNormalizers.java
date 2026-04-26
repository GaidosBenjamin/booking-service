package com.bgaidos.booking.util;

public final class AuthNormalizers {

    private AuthNormalizers() {
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
