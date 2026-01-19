package com.banking.system.auth.application.dto.result;

import com.banking.system.auth.domain.model.Role;

import java.util.UUID;

public record LoginResult(
        UUID id,
        String email,
        Role role,
        String token
) {
}
