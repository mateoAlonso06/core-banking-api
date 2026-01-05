package com.banking.system.auth.infraestructure.adapter.in.rest.dto;

import java.util.UUID;

public record RegisterUserResponse(
        UUID id,
        String email
) {
}
