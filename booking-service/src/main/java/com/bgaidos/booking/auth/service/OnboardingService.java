package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.OnboardingRequest;
import com.bgaidos.booking.api.auth.OnboardingResponse;
import com.bgaidos.booking.util.AuthNormalizers;
import com.bgaidos.booking.entity.Organization;
import com.bgaidos.booking.entity.Role;
import com.bgaidos.booking.repo.OrganizationRepository;
import com.bgaidos.booking.repo.RoleRepository;
import com.bgaidos.booking.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    public static final String DEFAULT_ROLE_NAME = "DEFAULT";
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public OnboardingResponse onboard(OnboardingRequest request) {
        var slug = AuthNormalizers.normalize(request.organizationSlug());
        log.info("onboarding organization slug={}", slug);
        if (organizationRepository.existsBySlug(slug)) {
            throw new BadRequestException("organization slug already exists: " + slug);
        }

        var organization = new Organization();
        organization.setSlug(slug);
        organization.setName(request.organizationName().trim());
        organizationRepository.save(organization);

        saveRole(organization.getId(), DEFAULT_ROLE_NAME,
            List.of("campers:read", "campers:write", "tiers:read", "buildings:read", "leaders:read",
                "rooms:read", "rooms:holds:write", "rooms:assignments:read", "conduct:read",
                "bookings:read", "bookings:write"));
        saveRole(organization.getId(), ADMIN_ROLE_NAME,
            List.of("tiers:write", "buildings:write", "leaders:write", "rooms:write",
                "rooms:assignments:write", "conduct:write"));

        log.info("onboarded organization id={} slug={}", organization.getId(), organization.getSlug());
        return new OnboardingResponse(organization.getId(), organization.getSlug());
    }

    private void saveRole(UUID tenantId, String name, List<String> permissions) {
        var role = new Role();
        role.setTenantId(tenantId);
        role.setName(name);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
