package com.bgaidos.booking.api.donation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DonationRequest(String name, @NotBlank String orgSlug, @NotNull @Positive BigDecimal amount, @NotBlank String currency) {}
