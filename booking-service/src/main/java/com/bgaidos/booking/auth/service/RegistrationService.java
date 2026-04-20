package com.bgaidos.booking.auth.service;

import com.bgaidos.booking.api.auth.RegisterRequest;
import com.bgaidos.booking.auth.util.AuthNormalizers;
import com.bgaidos.booking.data.entity.Role;
import com.bgaidos.booking.data.entity.User;
import com.bgaidos.booking.data.entity.UserProfile;
import com.bgaidos.booking.data.entity.UserRole;
import com.bgaidos.booking.data.repo.RoleRepository;
import com.bgaidos.booking.data.repo.UserProfileRepository;
import com.bgaidos.booking.data.repo.UserRepository;
import com.bgaidos.booking.data.repo.UserRoleRepository;
import com.bgaidos.booking.exception.BadRequestException;
import com.bgaidos.booking.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        if (userRepository.existsByTenantIdAndEmailIgnoreCase(organization.getId(), email)) {
            throw new BadRequestException("email already registered for this organization");
        }

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
        userProfileRepository.save(profile);

        roleRepository
            .findByTenantIdAndName(organization.getId(), OnboardingService.DEFAULT_ROLE_NAME)
            .ifPresent(role -> attachRole(user, role, organization.getId()));

        emailVerificationService.issue(user);
    }

    private void attachRole(User user, Role role, UUID tenantId) {
        var userRole = new UserRole();
        userRole.setTenantId(tenantId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }
}
