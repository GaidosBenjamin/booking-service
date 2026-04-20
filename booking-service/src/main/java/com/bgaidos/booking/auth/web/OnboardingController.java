package com.bgaidos.booking.auth.web;

import com.bgaidos.booking.api.auth.OnboardingRequest;
import com.bgaidos.booking.api.auth.OnboardingResponse;
import com.bgaidos.booking.auth.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse onboard(@Valid @RequestBody OnboardingRequest request) {
        return onboardingService.onboard(request);
    }
}
