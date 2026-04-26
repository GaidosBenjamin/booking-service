package com.bgaidos.booking.leader;

import com.bgaidos.booking.api.leader.LeaderCreateRequest;
import com.bgaidos.booking.api.leader.LeaderPatchRequest;
import com.bgaidos.booking.api.leader.LeaderResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.repo.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LeaderService {

    private final LeaderRepository leaderRepository;
    private final LeaderMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<LeaderResponse> list() {
        var leaders = leaderRepository.findAllForCurrentTenant();
        log.debug("list leaders tenant={} count={}", currentUser.tenantId(), leaders.size());
        return leaders.stream()
            .map(mapper::toResponse)
            .toList();
    }

    public LeaderResponse create(LeaderCreateRequest request) {
        var leader = mapper.toEntity(request);
        leader.setTenantId(currentUser.tenantId());
        var saved = leaderRepository.save(leader);
        log.info("created leader id={} tenant={} user={}", saved.getId(), currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(saved);
    }

    public LeaderResponse patch(UUID id, LeaderPatchRequest request) {
        var leader = leaderRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("leader not found: " + id));
        mapper.applyPatch(request, leader);
        log.info("patched leader id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
        return mapper.toResponse(leader);
    }

    public void delete(UUID id) {
        var leader = leaderRepository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("leader not found: " + id));
        leaderRepository.delete(leader);
        log.info("deleted leader id={} tenant={} user={}", id, currentUser.tenantId(), currentUser.userId());
    }
}
