package org.raflab.platforma_server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.request.LoginRequest;
import org.raflab.platforma_server.controllers.response.LoginResponse;
import org.raflab.platforma_server.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.username(), request.password());
        return new LoginResponse(result.token(), result.expiresAt(), result.role());
    }
}
