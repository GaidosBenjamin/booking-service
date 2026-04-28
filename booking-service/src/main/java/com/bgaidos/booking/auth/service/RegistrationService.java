package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.RegisterRequest;
import com.bgaidos.booking.util.AuthNormalizers;
import com.bgaidos.booking.entity.Role;
import com.bgaidos.booking.entity.User;
import com.bgaidos.booking.entity.UserProfile;
import com.bgaidos.booking.entity.UserRole;
import com.bgaidos.booking.repo.RoleRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.repo.UserRepository;
import com.bgaidos.booking.repo.UserRoleRepository;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

    private final TenantLookup tenantLookup;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public void register(RegisterRequest request) {
        var organization = tenantLookup.findOrganizationBySlug(request.organizationSlug())
            .orElseThrow(() -> new NotFoundException("organization not found: " + request.organizationSlug()));

        var email = AuthNormalizers.normalize(request.email());
        log.info("registering user in tenant={}", organization.getId());
        if (userRepository.existsByTenantIdAndEmailIgnoreCase(organization.getId(), email)) {
            throw new BadRequestException("email already registered for this organization");
        }

        var isFirstUser = userRepository.countByTenantId(organization.getId()) == 0;

        var user = new User();
        user.setTenantId(organization.getId());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        userRepository.save(user);

        var profile = new UserProfile();
        profile.setUser(user);
        profile.setTenantId(organization.getId());
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setPhone(request.phone().replaceAll("\\s+", ""));
        userProfileRepository.save(profile);

        attachRoleByName(user, organization.getId(), OnboardingService.DEFAULT_ROLE_NAME);
        if (isFirstUser) {
            log.info("first user of tenant={} — granting ADMIN role to user={}", organization.getId(), user.getId());
            attachRoleByName(user, organization.getId(), OnboardingService.ADMIN_ROLE_NAME);
        }

        emailVerificationService.issue(user);
        log.info("registered user={} tenant={} firstUser={}", user.getId(), organization.getId(), isFirstUser);
    }

    private void attachRoleByName(User user, UUID tenantId, String roleName) {
        roleRepository.findByTenantIdAndName(tenantId, roleName)
            .ifPresent(role -> attachRole(user, role, tenantId));
    }

    private void attachRole(User user, Role role, UUID tenantId) {
        var userRole = new UserRole();
        userRole.setTenantId(tenantId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }
}
