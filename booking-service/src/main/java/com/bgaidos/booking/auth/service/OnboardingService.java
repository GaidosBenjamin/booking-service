package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.OnboardingRequest;
import com.bgaidos.booking.api.auth.OnboardingResponse;
import com.bgaidos.booking.auth.util.AuthNormalizers;
import com.bgaidos.booking.data.entity.Organization;
import com.bgaidos.booking.data.entity.Role;
import com.bgaidos.booking.data.repo.OrganizationRepository;
import com.bgaidos.booking.data.repo.RoleRepository;
import com.bgaidos.booking.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    public static final String DEFAULT_ROLE_NAME = "DEFAULT";

    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public OnboardingResponse onboard(OnboardingRequest request) {
        var slug = AuthNormalizers.normalize(request.organizationSlug());
        if (organizationRepository.existsBySlug(slug)) {
            throw new BadRequestException("organization slug already exists: " + slug);
        }

        var organization = new Organization();
        organization.setSlug(slug);
        organization.setName(request.organizationName().trim());
        organizationRepository.save(organization);

        var defaultRole = new Role();
        defaultRole.setTenantId(organization.getId());
        defaultRole.setName(DEFAULT_ROLE_NAME);
        //TODO setup list of default permissions here
        defaultRole.setPermissions(List.of());
        roleRepository.save(defaultRole);

        return new OnboardingResponse(organization.getId(), organization.getSlug());
    }
}
