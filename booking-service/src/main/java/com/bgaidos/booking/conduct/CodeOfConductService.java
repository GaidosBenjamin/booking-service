package com.bgaidos.booking.conduct;

import com.bgaidos.booking.api.conduct.CodeOfConductRequest;
import com.bgaidos.booking.api.conduct.CodeOfConductResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.CodeOfConduct;
import com.bgaidos.booking.repo.CodeOfConductRepository;
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
public class CodeOfConductService {

    private final CodeOfConductRepository repository;
    private final CodeOfConductMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<CodeOfConductResponse> list() {
        var codes = repository.findAllForCurrentTenant();
        log.debug("list code-of-conduct tenant={} count={}", currentUser.tenantId(), codes.size());
        return codes.stream()
            .map(mapper::toResponse)
            .toList();
    }

    public CodeOfConductResponse create(CodeOfConductRequest request) {
        var active = request.active() == null || request.active();
        if (active) {
            repository.deactivateAllActiveForCurrentTenant();
        }
        var entity = new CodeOfConduct();
        entity.setTenantId(currentUser.tenantId());
        entity.setContent(request.content());
        entity.setActive(active);
        var saved = repository.save(entity);
        log.info("created code-of-conduct id={} tenant={} active={}",
            saved.getId(), currentUser.tenantId(), active);
        return mapper.toResponse(saved);
    }

    public CodeOfConductResponse replace(UUID id, CodeOfConductRequest request) {
        var entity = repository.findByIdForCurrentTenant(id)
            .orElseThrow(() -> new NotFoundException("code of conduct not found: " + id));
        var active = request.active() == null || request.active();
        if (active) {
            repository.deactivateAllActiveForCurrentTenant();
        }
        entity.setContent(request.content());
        entity.setActive(active);
        log.info("replaced code-of-conduct id={} tenant={} active={}",
            entity.getId(), currentUser.tenantId(), active);
        return mapper.toResponse(entity);
    }
}
