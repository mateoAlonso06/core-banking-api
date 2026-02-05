package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.auth.application.dto.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 8)
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
