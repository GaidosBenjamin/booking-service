package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.data.repo.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class AuthoritiesResolver {

    private final UserRoleRepository userRoleRepository;

    public List<GrantedAuthority> authoritiesFor(UUID userId) {
        var roles = userRoleRepository.findRolesByUserId(userId);
        return roles.stream()
            .flatMap(role -> Stream.concat(
                Stream.of("ROLE_" + role.getName()),
                Optional.ofNullable(role.getPermissions()).orElse(List.of()).stream()))
            .distinct()
            .<GrantedAuthority>map(SimpleGrantedAuthority::new)
            .toList();
    }
}
