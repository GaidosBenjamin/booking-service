package com.bgaidos.booking.payments;

public enum StripeEntityType {
    BOOKING("Tabara Kids 2026 — Cazare"),
    DONATION("Tabara Kids 2026 — Donatie");

    public final String description;

    StripeEntityType(String description) {
        this.description = description;
    }
}
