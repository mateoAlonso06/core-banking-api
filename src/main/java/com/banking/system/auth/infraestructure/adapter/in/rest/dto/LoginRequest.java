package com.banking.system.auth.infraestructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8)
        String password
) {
}
