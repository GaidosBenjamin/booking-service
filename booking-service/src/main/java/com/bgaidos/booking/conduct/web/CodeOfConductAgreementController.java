package com.bgaidos.booking.conduct.web;

import com.bgaidos.booking.api.conduct.CodeOfConductAgreementRequest;
import com.bgaidos.booking.api.conduct.CodeOfConductAgreementResponse;
import com.bgaidos.booking.conduct.CodeOfConductAgreementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/code-of-conduct/agreements")
@RequiredArgsConstructor
public class CodeOfConductAgreementController {

    private final CodeOfConductAgreementService service;

    @GetMapping
    @PreAuthorize("hasAuthority('conduct:read')")
    public List<CodeOfConductAgreementResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('conduct:read')")
    public CodeOfConductAgreementResponse create(@Valid @RequestBody CodeOfConductAgreementRequest request) {
        return service.create(request);
    }
}
