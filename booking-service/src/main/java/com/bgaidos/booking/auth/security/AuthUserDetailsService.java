package com.bgaidos.booking.auth.security;

import com.bgaidos.booking.auth.security.model.AuthUser;
import com.bgaidos.booking.auth.service.AuthoritiesResolver;
import com.bgaidos.booking.data.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthoritiesResolver authoritiesResolver;

    public AuthUser loadByTenantAndEmail(UUID tenantId, String email) {
        var user = userRepository
            .findByTenantIdAndEmailIgnoreCase(tenantId, email)
            .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        var authorities = authoritiesResolver.authoritiesFor(user.getId());
        return new AuthUser(user, authorities);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        throw new UnsupportedOperationException(
            "tenant-agnostic lookup is ambiguous; use loadByTenantAndEmail(tenantId, email)");
    }
}
