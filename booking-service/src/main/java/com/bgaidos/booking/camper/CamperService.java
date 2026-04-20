package com.bgaidos.booking.camper;

import com.bgaidos.booking.api.camper.CamperCreateRequest;
import com.bgaidos.booking.api.camper.CamperPatchRequest;
import com.bgaidos.booking.api.camper.CamperResponse;
import com.bgaidos.booking.auth.security.session.CurrentUser;
import com.bgaidos.booking.data.entity.CamperStatus;
import com.bgaidos.booking.data.repo.CamperRepository;
import com.bgaidos.booking.data.repo.UserRepository;
import com.bgaidos.booking.exception.BadRequestException;
import com.bgaidos.booking.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CamperService {

    private final CamperRepository camperRepository;
    private final UserRepository userRepository;
    private final CamperMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<CamperResponse> list() {
        return camperRepository.findAllForCurrentUser().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public CamperResponse create(CamperCreateRequest request) {
        var camper = mapper.toEntity(request);
        camper.setTenantId(currentUser.tenantId());
        camper.setParentUser(userRepository.getReferenceById(currentUser.userId()));
        camper.setStatus(CamperStatus.NEEDS_BED);
        var saved = camperRepository.save(camper);
        return mapper.toResponse(saved);
    }

    public CamperResponse patch(UUID id, CamperPatchRequest request) {
        var camper = camperRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("camper not found: " + id));
        mapper.applyPatch(request, camper);
        return mapper.toResponse(camper);
    }

    public void delete(UUID id) {
        var camper = camperRepository.findByIdForCurrentUser(id)
            .orElseThrow(() -> new NotFoundException("camper not found: " + id));
        if (camper.getStatus() == CamperStatus.PAYMENT_SUCCESS) {
            throw new BadRequestException("camper paid — cannot delete");
        }
        camperRepository.delete(camper);
    }
}
