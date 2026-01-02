package com.banking.system.auth.infraestructure.adapter.in.rest.dto;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String email,
        String accesToken
) {
}
