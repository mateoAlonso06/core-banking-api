package com.banking.system.auth.infraestructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotBlank(message = "First name is required") @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 100)
        String lastName,

        @NotBlank(message = "Document type is required") @Size(max = 20)
        String documentType,

        @NotBlank(message = "Document number is required") @Size(max = 50)
        String documentNumber,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        @NotBlank(message = "Phone is required") @Size(max = 20)
        String phone,

        @Size(max = 255)
        String address,

        @Size(max = 100)
        String city,

        @Size(max = 2)
        String country
) {}
