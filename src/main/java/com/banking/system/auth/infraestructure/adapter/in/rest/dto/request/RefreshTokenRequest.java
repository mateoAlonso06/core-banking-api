package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {}