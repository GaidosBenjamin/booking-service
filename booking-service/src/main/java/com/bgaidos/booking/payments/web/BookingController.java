package com.bgaidos.booking.payments.web;

import com.bgaidos.booking.api.booking.BookingCreateRequest;
import com.bgaidos.booking.api.booking.BookingResponse;
import com.bgaidos.booking.payments.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('bookings:write')")
    public BookingResponse create(@Valid @RequestBody(required = false) BookingCreateRequest request) {
        return bookingService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('bookings:read')")
    public List<BookingResponse> list() {
        return bookingService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('bookings:read')")
    public BookingResponse get(@PathVariable UUID id) {
        return bookingService.get(id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('bookings:write')")
    public void cancel(@PathVariable UUID id) {
        bookingService.cancel(id);
    }
}
