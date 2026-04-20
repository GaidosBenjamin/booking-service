package com.bgaidos.booking.auth.security.model;

import com.bgaidos.booking.data.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AuthUser implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public boolean isEnabled() {
        return user.isEmailVerified();
    }

    public UUID getUserId() {
        return user.getId();
    }

    public UUID getTenantId() {
        return user.getTenantId();
    }
}
