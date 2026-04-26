package com.bgaidos.booking.building;

import com.bgaidos.booking.api.building.BuildingCreateRequest;
import com.bgaidos.booking.api.building.BuildingPatchRequest;
import com.bgaidos.booking.api.building.BuildingResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.Building;
import com.bgaidos.booking.entity.Tier;
import com.bgaidos.booking.repo.BuildingRepository;
import com.bgaidos.booking.repo.TierRepository;
import com.bgaidos.booking.member.MembershipService;
import com.bgaidos.booking.tier.TierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final TierRepository tierRepository;
    private final MembershipService membershipService;
    private final BuildingMapper mapper;
    private final TierMapper tierMapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<BuildingResponse> list(String gender, int age) {
        var now = Instant.now();
        var buildings = buildingRepository.findForCurrentTenantFiltered(gender, age, now);
        var isMember = membershipService.isMember();
        log.debug("list buildings tenant={} gender={} age={} count={} member={}",
            currentUser.tenantId(), gender, age, buildings.size(), isMember);
        return buildings.stream()
            .map(b -> toResponse(b, isMember, gender, age, now))
            .toList();
    }

    public BuildingResponse create(BuildingCreateRequest request) {
        var building = mapper.toEntity(request);
        building.setTenantId(currentUser.tenantId());
        building.setTier(resolveTier(request.tierId()));
        var saved = buildingRepository.save(building);
        log.info("created building id={} tenant={} user={}", saved.getId(), currentUser.tenantId(), currentUser.userId());
        return toResponse(saved, membershipService.isMember(), null, 0, Instant.now());
    }

    public BuildingResponse patch(UUID id, BuildingPatchRequest request) {
        var building = buildingRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("building not found: " + id));
        mapper.applyPatch(request, building);
        if (request.tierId() != null) {
            building.setTier(resolveTier(request.tierId()));
        }
        log.info("patched building id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
        return toResponse(building, membershipService.isMember(), null, 0, Instant.now());
    }

    public void delete(UUID id) {
        var building = buildingRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("building not found: " + id));
        buildingRepository.delete(building);
        log.info("deleted building id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
    }

    private BuildingResponse toResponse(Building building, boolean isMember, String gender, int age, Instant now) {
        var tier = tierMapper.toResponse(building.getTier(), isMember);
        var isFull = gender != null && !buildingRepository.hasAvailableRooms(building.getId(), gender, age, now);
        return mapper.toResponse(building, tier, isFull);
    }

    private Tier resolveTier(UUID tierId) {
        return tierRepository.findByIdForCurrentTenant(tierId)
            .orElseThrow(() -> new NotFoundException("tier not found: " + tierId));
    }
}
