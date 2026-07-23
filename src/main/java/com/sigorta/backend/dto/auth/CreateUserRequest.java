package com.sigorta.backend.dto.auth;

import com.sigorta.backend.entity.Role;

public record CreateUserRequest(
        String username,
        String password,
        Role role
) {
}
