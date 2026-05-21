package com.bgaidos.booking.util;

import java.util.Locale;

public final class LocaleResolver {

    private LocaleResolver() {
    }

    public static Locale resolve(String language) {
        if ("en".equals(language)) return Locale.ENGLISH;
        return Locale.of("ro");
    }
}
