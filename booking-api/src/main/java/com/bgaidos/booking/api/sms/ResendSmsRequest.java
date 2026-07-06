package com.bgaidos.booking.api.sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ResendSmsRequest(
    @NotEmpty List<String> phones,
    @NotBlank @Size(max = 480) String textEn,
    @NotBlank @Size(max = 480) String textRo
) {
}
