package com.bgaidos.booking.tier.web;

import com.bgaidos.booking.api.tier.TierCreateRequest;
import com.bgaidos.booking.api.tier.TierPatchRequest;
import com.bgaidos.booking.api.tier.TierResponse;
import com.bgaidos.booking.tier.TierService;
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
@RequestMapping("/api/tiers")
@RequiredArgsConstructor
public class TierController {

    private final TierService tierService;

    @GetMapping
    @PreAuthorize("hasAuthority('tiers:read')")
    public List<TierResponse> list() {
        return tierService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('tiers:write')")
    public TierResponse create(@Valid @RequestBody TierCreateRequest request) {
        return tierService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('tiers:write')")
    public TierResponse patch(@PathVariable UUID id, @Valid @RequestBody TierPatchRequest request) {
        return tierService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('tiers:write')")
    public void delete(@PathVariable UUID id) {
        tierService.delete(id);
    }
}
