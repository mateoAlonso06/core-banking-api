package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.auth.application.dto.command.VerifyTwoFactorCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyTwoFactorRequest(
        @NotBlank
        String sessionToken,

        @NotBlank
        @Size(min = 6, max = 6, message = "Code must be exactly 6 digits")
        @Pattern(regexp = "^[0-9]{6}$", message = "Code must contain only digits")
        String code
) {
    public VerifyTwoFactorCommand toCommand() {
        return new VerifyTwoFactorCommand(sessionToken, code);
    }
}
