package org.raflab.platforma_server.controllers.response;

import java.time.Instant;

import org.raflab.platforma_server.model.Role;

public record LoginResponse(String token, Instant expiresAt, Role role) {
}
