package com.bgaidos.booking.building.web;

import com.bgaidos.booking.api.building.BuildingCreateRequest;
import com.bgaidos.booking.api.building.BuildingPatchRequest;
import com.bgaidos.booking.api.building.BuildingResponse;
import com.bgaidos.booking.building.BuildingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @PreAuthorize("hasAuthority('buildings:read')")
    public List<BuildingResponse> list(
        @RequestParam @Pattern(regexp = "male|female", message = "must be 'male' or 'female'") String gender,
        @RequestParam @Min(0) int age
    ) {
        return buildingService.list(gender, age);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('buildings:write')")
    public BuildingResponse create(@Valid @RequestBody BuildingCreateRequest request) {
        return buildingService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('buildings:write')")
    public BuildingResponse patch(@PathVariable UUID id, @Valid @RequestBody BuildingPatchRequest request) {
        return buildingService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('buildings:write')")
    public void delete(@PathVariable UUID id) {
        buildingService.delete(id);
    }
}
