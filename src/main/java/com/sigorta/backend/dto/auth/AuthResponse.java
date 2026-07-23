package com.sigorta.backend.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role
) {
}
