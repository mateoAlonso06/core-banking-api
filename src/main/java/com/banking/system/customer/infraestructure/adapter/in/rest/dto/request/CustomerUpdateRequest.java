package com.banking.system.customer.infraestructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CustomerUpdateRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Document type is required")
        @Size(max = 20, message = "Document type must not exceed 20 characters")
        String documentType,

        @NotBlank(message = "Document number is required")
        @Size(max = 50, message = "Document number must not exceed 50 characters")
        String documentNumber,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        @NotBlank
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters") @NotBlank
        String city,

        @Size(min = 2, max = 2, message = "Country must be a 2-character ISO code") @NotBlank
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be uppercase ISO 3166-1 alpha-2 code")
        String country
) {
}
