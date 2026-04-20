package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.AuthTokenResponse;
import com.bgaidos.booking.api.auth.RefreshTokenRequest;
import com.bgaidos.booking.auth.security.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RefreshTokenService refreshTokenService;
    private final AuthoritiesResolver authoritiesResolver;
    private final JwtService jwtService;

    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        var rotated = refreshTokenService.rotate(request.refreshToken());
        var user = rotated.user();
        var authorities = authoritiesResolver.authoritiesFor(user.getId());
        var authUser = new AuthUser(user, authorities);
        var issued = jwtService.issue(authUser);

        return new AuthTokenResponse(
            issued.value(),
            "Bearer",
            issued.expiresInSeconds(),
            rotated.refresh().value(),
            rotated.refresh().expiresInSeconds(),
            user.getId(),
            user.getTenantId());
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
