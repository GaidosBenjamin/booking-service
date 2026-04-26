package com.bgaidos.booking.member;

import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.repo.MemberRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MembershipService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final CurrentUser currentUser;

    public boolean isMember() {
        var email = currentUser.email().toLowerCase();
        var phone = userProfileRepository.findByUserId(currentUser.userId())
            .map(p -> p.getPhone() != null ? p.getPhone().toLowerCase() : null)
            .orElse(null);
        return memberRepository.existsByTenantIdAndEmailOrPhone(
            currentUser.tenantId(), email, phone);
    }
}
