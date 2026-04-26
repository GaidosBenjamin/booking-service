package com.bgaidos.booking.camper.web;

import com.bgaidos.booking.api.camper.CamperCreateRequest;
import com.bgaidos.booking.api.camper.CamperPatchRequest;
import com.bgaidos.booking.api.camper.CamperResponse;
import com.bgaidos.booking.camper.CamperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campers")
@RequiredArgsConstructor
public class CamperController {

    private final CamperService camperService;

    @GetMapping
    @PreAuthorize("hasAuthority('campers:read')")
    public List<CamperResponse> list() {
        return camperService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('campers:write')")
    public CamperResponse create(@Valid @RequestBody CamperCreateRequest request) {
        return camperService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('campers:write')")
    public CamperResponse patch(@PathVariable UUID id, @Valid @RequestBody CamperPatchRequest request) {
        return camperService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('campers:write')")
    public void delete(@PathVariable UUID id) {
        camperService.delete(id);
    }
}
