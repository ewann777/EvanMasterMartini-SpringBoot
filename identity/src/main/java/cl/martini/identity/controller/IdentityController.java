package cl.martini.identity.controller;

import jakarta.validation.Valid;
import cl.martini.common.Account;
import cl.martini.identity.dto.IdentityDto.*;
import cl.martini.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class IdentityController {
    private final IdentityService identity;

    @PostMapping("/api/v1/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    Profile register(@Valid @RequestBody Register request) {
        return identity.register(request);
    }

    @PostMapping("/api/v1/auth/login")
    Session login(@Valid @RequestBody Login request) {
        return identity.login(request);
    }

    @PostMapping("/api/v1/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        identity.logout(authorization.substring(7));
    }

    @GetMapping("/api/v1/profile/me")
    Profile profile(@AuthenticationPrincipal Account account) {
        return identity.getProfile(account.id());
    }

    @PatchMapping("/api/v1/profile/me")
    Profile update(@AuthenticationPrincipal Account account, @Valid @RequestBody ProfilePatch patch) {
        return identity.updateProfile(account.id(), patch);
    }

    @GetMapping("/internal/session")
    Account session(@AuthenticationPrincipal Account account) {
        return account;
    }
}
