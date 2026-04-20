package com.bgaidos.booking.auth.security;

import com.bgaidos.booking.auth.security.model.AuthUser;
import com.bgaidos.booking.auth.security.model.TenantAuthenticationToken;
import com.bgaidos.booking.data.repo.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantAuthenticationProvider implements AuthenticationProvider {

    private final OrganizationRepository organizationRepository;
    private final AuthUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        var token = (TenantAuthenticationToken) authentication;

        var organization = organizationRepository.findBySlug(token.getOrganizationSlug())
            .orElseThrow(() -> new BadCredentialsException("invalid credentials"));

        AuthUser authUser;
        try {
            authUser = userDetailsService.loadByTenantAndEmail(organization.getId(), token.getPrincipal().toString());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("invalid credentials");
        }

        if (!passwordEncoder.matches(token.getCredentials().toString(), authUser.getPassword())) {
            throw new BadCredentialsException("invalid credentials");
        }

        if (!authUser.isEnabled()) {
            throw new DisabledException("email not verified");
        }

        return new TenantAuthenticationToken(authUser, authUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TenantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
