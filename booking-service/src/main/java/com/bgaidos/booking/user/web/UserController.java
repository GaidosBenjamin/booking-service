package com.bgaidos.booking.user.web;

import com.bgaidos.booking.api.user.UserMeResponse;
import com.bgaidos.booking.api.user.UserPatchRequest;
import com.bgaidos.booking.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserMeResponse me() {
        return userService.me();
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserMeResponse patch(@Valid @RequestBody UserPatchRequest request) {
        return userService.patch(request);
    }
}
