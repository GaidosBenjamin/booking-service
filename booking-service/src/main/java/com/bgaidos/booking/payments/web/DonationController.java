package com.bgaidos.booking.payments.web;

import com.bgaidos.booking.api.donation.DonationRequest;
import com.bgaidos.booking.api.donation.DonationResponse;
import com.bgaidos.booking.payments.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResponse create(@Valid @RequestBody DonationRequest request) {
        return donationService.create(request);
    }

    @GetMapping("/{id}")
    public DonationResponse get(@PathVariable UUID id) {
        return donationService.get(id);
    }
}
