package com.sigorta.backend.dto.auth;

public record UserResponse(
        Long id,
        String username,
        String role
) {
}
