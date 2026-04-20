package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.AuthTokenResponse;
import com.bgaidos.booking.api.auth.LoginRequest;
import com.bgaidos.booking.auth.security.model.AuthUser;
import com.bgaidos.booking.auth.security.model.TenantAuthenticationToken;
import com.bgaidos.booking.auth.util.AuthNormalizers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthTokenResponse login(LoginRequest request) {
        var token = new TenantAuthenticationToken(
            AuthNormalizers.normalize(request.organizationSlug()),
            AuthNormalizers.normalize(request.email()),
            request.password());

        var authenticated = authenticationManager.authenticate(token);
        var authUser = (AuthUser) authenticated.getPrincipal();

        var issued = jwtService.issue(authUser);
        var refresh = refreshTokenService.issueForNewSession(authUser.getUser());

        return new AuthTokenResponse(
            issued.value(),
            "Bearer",
            issued.expiresInSeconds(),
            refresh.value(),
            refresh.expiresInSeconds(),
            authUser.getUserId(),
            authUser.getTenantId());
    }
}
