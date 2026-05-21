package com.bgaidos.booking.api.sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BroadcastSmsRequest(
    @NotBlank @Size(max = 480) String textEn,
    @NotBlank @Size(max = 480) String textRo
) {
}
