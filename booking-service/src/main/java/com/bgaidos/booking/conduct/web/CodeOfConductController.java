package com.bgaidos.booking.conduct.web;

import com.bgaidos.booking.api.conduct.CodeOfConductRequest;
import com.bgaidos.booking.api.conduct.CodeOfConductResponse;
import com.bgaidos.booking.conduct.CodeOfConductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/code-of-conduct")
@RequiredArgsConstructor
public class CodeOfConductController {

    private final CodeOfConductService service;

    @GetMapping
    @PreAuthorize("hasAuthority('conduct:read')")
    public List<CodeOfConductResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('conduct:write')")
    public CodeOfConductResponse create(@Valid @RequestBody CodeOfConductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('conduct:write')")
    public CodeOfConductResponse replace(@PathVariable UUID id, @Valid @RequestBody CodeOfConductRequest request) {
        return service.replace(id, request);
    }
}
