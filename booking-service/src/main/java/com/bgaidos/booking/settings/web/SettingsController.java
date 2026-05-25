package com.bgaidos.booking.settings.web;

import com.bgaidos.booking.api.settings.RegistrationStatusRequest;
import com.bgaidos.booking.api.settings.RegistrationStatusResponse;
import com.bgaidos.booking.settings.RegistrationGate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final RegistrationGate registrationGate;

    @GetMapping("/registrations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public RegistrationStatusResponse getRegistrationStatus() {
        return new RegistrationStatusResponse(registrationGate.isEnabled(), registrationGate.isMemberOnly());
    }

    @PatchMapping("/registrations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public RegistrationStatusResponse setRegistrationStatus(@RequestBody RegistrationStatusRequest request) {
        if (request.enabled() != null) registrationGate.setEnabled(request.enabled());
        if (request.memberOnly() != null) registrationGate.setMemberOnly(request.memberOnly());
        return new RegistrationStatusResponse(registrationGate.isEnabled(), registrationGate.isMemberOnly());
    }
}
