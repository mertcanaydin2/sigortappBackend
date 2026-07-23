package com.sigorta.backend.dto.auth;

public record LoginRequest(
        String username,
        String password
) {
}
