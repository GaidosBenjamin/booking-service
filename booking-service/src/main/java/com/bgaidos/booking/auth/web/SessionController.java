package com.bgaidos.booking.auth.web;

import com.bgaidos.booking.api.auth.AuthTokenResponse;
import com.bgaidos.booking.api.auth.LoginRequest;
import com.bgaidos.booking.api.auth.RefreshTokenRequest;
import com.bgaidos.booking.auth.service.LoginService;
import com.bgaidos.booking.auth.service.SessionService;
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
public class SessionController {

    private final LoginService loginService;
    private final SessionService sessionService;

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return sessionService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        sessionService.logout(request);
    }
}
