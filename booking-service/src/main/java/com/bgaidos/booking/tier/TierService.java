package com.bgaidos.booking.tier;

import com.bgaidos.booking.api.tier.TierCreateRequest;
import com.bgaidos.booking.api.tier.TierPatchRequest;
import com.bgaidos.booking.api.tier.TierResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.BadRequestException;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.repo.TierRepository;
import com.bgaidos.booking.member.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TierService {

    private final TierRepository tierRepository;
    private final MembershipService membershipService;
    private final TierMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<TierResponse> list() {
        var tiers = tierRepository.findAllForCurrentTenant();
        var isMember = membershipService.isMember();
        log.debug("list tiers tenant={} count={} member={}", currentUser.tenantId(), tiers.size(), isMember);
        return tiers.stream()
            .map(t -> mapper.toResponse(t, isMember))
            .toList();
    }

    public TierResponse create(TierCreateRequest request) {
        assertPricesValid(request.basePrice(), request.discountPrice());
        var tier = mapper.toEntity(request);
        tier.setTenantId(currentUser.tenantId());
        var saved = tierRepository.save(tier);
        log.info("created tier id={} tenant={} user={}", saved.getId(), currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(saved, membershipService.isMember());
    }

    public TierResponse patch(UUID id, TierPatchRequest request) {
        var tier = tierRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("tier not found: " + id));
        mapper.applyPatch(request, tier);
        assertPricesValid(tier.getBasePrice(), tier.getDiscountPrice());
        log.info("patched tier id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(tier, membershipService.isMember());
    }

    public void delete(UUID id) {
        var tier = tierRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("tier not found: " + id));
        tier.setDeletedAt(Instant.now());
        log.info("soft-deleted tier id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
    }

    private static void assertPricesValid(BigDecimal basePrice, BigDecimal discountPrice) {
        if (basePrice == null || discountPrice == null) {
            return;
        }
        if (discountPrice.compareTo(basePrice) > 0) {
            throw new BadRequestException("discountPrice must not exceed basePrice");
        }
    }
}
