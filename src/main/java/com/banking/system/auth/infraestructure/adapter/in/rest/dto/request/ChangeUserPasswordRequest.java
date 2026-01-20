package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.auth.application.dto.command.ChangeUserPasswordCommand;
import jakarta.validation.constraints.NotBlank;

public record ChangeUserPasswordRequest(
        @NotBlank
        String oldPassword,
        @NotBlank
        String newPassword
) {
    public ChangeUserPasswordCommand toCommand() {
        return new ChangeUserPasswordCommand(
                this.oldPassword,
                this.newPassword
        );
    }
}
