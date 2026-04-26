package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.AuthTokenResponse;
import com.bgaidos.booking.api.auth.LoginRequest;
import com.bgaidos.booking.auth.service.model.AuthUser;
import com.bgaidos.booking.auth.service.model.TenantAuthenticationToken;
import com.bgaidos.booking.util.AuthNormalizers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthTokenResponse login(LoginRequest request) {
        var slug = AuthNormalizers.normalize(request.organizationSlug());
        log.info("login attempt slug={}", slug);

        var token = new TenantAuthenticationToken(
            slug,
            AuthNormalizers.normalize(request.email()),
            request.password());

        var authenticated = authenticationManager.authenticate(token);
        var authUser = (AuthUser) authenticated.getPrincipal();

        if (!authUser.getUser().isEmailVerified()) {
            log.warn("login rejected — email not verified user={}", authUser.getUserId());
            throw new BadCredentialsException("email not verified");
        }

        var issued = jwtService.issue(authUser);
        var refresh = refreshTokenService.issueForNewSession(authUser.getUser());

        log.info("login success user={} tenant={}", authUser.getUserId(), authUser.getTenantId());

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
