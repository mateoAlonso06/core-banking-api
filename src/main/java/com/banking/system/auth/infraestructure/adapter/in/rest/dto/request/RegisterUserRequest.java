package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.auth.application.dto.command.RegisterCommand;
import com.banking.system.common.infraestructure.utils.SanitizeHtml;
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
        @SanitizeHtml
        String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 100)
        @SanitizeHtml
        String lastName,

        @NotBlank(message = "Document type is required") @Size(max = 20)
        String documentType,

        @NotBlank(message = "Document number is required") @Size(max = 50)
        String documentNumber,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        @NotBlank(message = "Phone is required") @Size(max = 20)
        @SanitizeHtml
        String phone,

        @Size(max = 255)
        @NotBlank
        @SanitizeHtml
        String address,

        @Size(max = 100)
        @NotBlank
        @SanitizeHtml
        String city,

        @Size(max = 2)
        @NotBlank
        @SanitizeHtml
        String country
) {

    public RegisterCommand toCommand() {
        return new RegisterCommand(
                this.email,
                this.password,
                this.firstName,
                this.lastName,
                this.documentType,
                this.documentNumber,
                this.birthDate,
                this.phone,
                this.address,
                this.city,
                this.country
        );
    }
}
