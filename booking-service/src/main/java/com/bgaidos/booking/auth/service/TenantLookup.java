package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.auth.util.AuthNormalizers;
import com.bgaidos.booking.data.entity.Organization;
import com.bgaidos.booking.data.entity.User;
import com.bgaidos.booking.data.repo.OrganizationRepository;
import com.bgaidos.booking.data.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantLookup {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public Optional<Organization> findOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(AuthNormalizers.normalize(slug));
    }

    public Optional<User> findUserByEmail(UUID tenantId, String email) {
        return userRepository.findByTenantIdAndEmailIgnoreCase(tenantId, AuthNormalizers.normalize(email));
    }
}
