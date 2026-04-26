package com.bgaidos.booking.auth.service.session;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    public UUID userId() {
        return UUID.fromString(authentication().getName());
    }

    public UUID tenantId() {
        return UUID.fromString(token().getToken().getClaimAsString("tid"));
    }

    public String email() {
        var email = token().getToken().getClaimAsString("email");
        if (email == null) {
            throw new IllegalStateException("JWT is missing 'email' claim");
        }
        return email;
    }

    private JwtAuthenticationToken token() {
        var authentication = authentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            return jwt;
        }
        throw new IllegalStateException("no JWT authentication in security context");
    }

    private Authentication authentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("no authenticated principal in security context");
        }
        return authentication;
    }
}
