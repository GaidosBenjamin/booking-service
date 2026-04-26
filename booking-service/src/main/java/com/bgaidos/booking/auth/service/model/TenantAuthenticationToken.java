package com.bgaidos.booking.auth.service.model;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class TenantAuthenticationToken extends AbstractAuthenticationToken {

    private final String organizationSlug;
    private final Object principal;
    private Object credentials;

    public TenantAuthenticationToken(String organizationSlug, String email, String password) {
        super((Collection<? extends GrantedAuthority>) null);
        this.organizationSlug = organizationSlug;
        this.principal = email;
        this.credentials = password;
        setAuthenticated(false);
    }

    public TenantAuthenticationToken(
        AuthUser principal,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.organizationSlug = null;
        this.principal = principal;
        this.credentials = null;
        super.setAuthenticated(true);
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                "use the authenticated constructor to produce a trusted token");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
