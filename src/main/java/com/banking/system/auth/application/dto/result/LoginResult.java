package com.banking.system.auth.application.dto.result;

import com.banking.system.auth.domain.model.RoleName;

import java.util.UUID;

public record LoginResult(
        UUID id,
        String email,
        RoleName role,
        String token
) {
}
