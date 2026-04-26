package com.bgaidos.booking.user;

import com.bgaidos.booking.api.user.UserMeResponse;
import com.bgaidos.booking.api.user.UserPatchRequest;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.entity.UserProfile;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.member.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CurrentUser currentUser;
    private final UserProfileRepository userProfileRepository;
    private final MembershipService membershipService;

    @Transactional(readOnly = true)
    public UserMeResponse me() {
        var profile = userProfileRepository.findByUserId(currentUser.userId()).orElse(null);
        return toResponse(profile);
    }

    @Transactional
    public UserMeResponse patch(UserPatchRequest request) {
        var profile = userProfileRepository.findByUserId(currentUser.userId()).orElseThrow();
        if (request.firstName() != null) profile.setFirstName(request.firstName().trim());
        if (request.lastName() != null) profile.setLastName(request.lastName().trim());
        if (request.phone() != null) profile.setPhone(request.phone().trim());
        log.info("patched user profile userId={}", currentUser.userId());
        return toResponse(profile);
    }

    private UserMeResponse toResponse(UserProfile profile) {
        return new UserMeResponse(
            currentUser.userId(),
            currentUser.tenantId(),
            currentUser.email(),
            profile != null ? profile.getFirstName() : null,
            profile != null ? profile.getLastName() : null,
            profile != null ? profile.getPhone() : null,
            membershipService.isMember()
        );
    }
}
