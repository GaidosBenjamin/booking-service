package com.bgaidos.booking.conduct;

import com.bgaidos.booking.api.conduct.CodeOfConductAgreementRequest;
import com.bgaidos.booking.api.conduct.CodeOfConductAgreementResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.common.exception.NotFoundException;
import com.bgaidos.booking.entity.CodeOfConductAgreement;
import com.bgaidos.booking.repo.CodeOfConductAgreementRepository;
import com.bgaidos.booking.repo.CodeOfConductRepository;
import com.bgaidos.booking.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CodeOfConductAgreementService {

    private final CodeOfConductAgreementRepository agreementRepository;
    private final CodeOfConductRepository codeRepository;
    private final UserRepository userRepository;
    private final CodeOfConductAgreementMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<CodeOfConductAgreementResponse> list() {
        return agreementRepository.findAllForCurrentUser().stream()
            .map(mapper::toResponse)
            .toList();
    }

    public CodeOfConductAgreementResponse create(CodeOfConductAgreementRequest request) {
        var code = codeRepository.findByIdForCurrentTenant(request.codeOfConductId())
            .orElseThrow(() -> new NotFoundException("code of conduct not found: " + request.codeOfConductId()));

        var userId = currentUser.userId();
        var existing = agreementRepository.findByUserIdAndCodeOfConductId(userId, code.getId()).orElse(null);
        if (existing != null) {
            log.debug("code-of-conduct agreement idempotent re-hit user={} code={}", userId, code.getId());
            return mapper.toResponse(existing);
        }

        var agreement = new CodeOfConductAgreement();
        agreement.setTenantId(currentUser.tenantId());
        agreement.setUser(userRepository.getReferenceById(userId));
        agreement.setCodeOfConduct(code);
        agreement.setAgreedOn(Instant.now());
        var saved = agreementRepository.save(agreement);
        log.info("created code-of-conduct agreement id={} user={} code={}",
            saved.getId(), userId, code.getId());
        return mapper.toResponse(saved);
    }
}
