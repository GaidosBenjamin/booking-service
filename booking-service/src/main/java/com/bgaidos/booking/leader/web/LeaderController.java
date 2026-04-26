package com.bgaidos.booking.leader.web;

import com.bgaidos.booking.api.leader.LeaderCreateRequest;
import com.bgaidos.booking.api.leader.LeaderPatchRequest;
import com.bgaidos.booking.api.leader.LeaderResponse;
import com.bgaidos.booking.leader.LeaderService;
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
@RequestMapping("/api/leaders")
@RequiredArgsConstructor
public class LeaderController {

    private final LeaderService leaderService;

    @GetMapping
    @PreAuthorize("hasAuthority('leaders:read')")
    public List<LeaderResponse> list() {
        return leaderService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('leaders:write')")
    public LeaderResponse create(@Valid @RequestBody LeaderCreateRequest request) {
        return leaderService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('leaders:write')")
    public LeaderResponse patch(@PathVariable UUID id, @Valid @RequestBody LeaderPatchRequest request) {
        return leaderService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('leaders:write')")
    public void delete(@PathVariable UUID id) {
        leaderService.delete(id);
    }
}
