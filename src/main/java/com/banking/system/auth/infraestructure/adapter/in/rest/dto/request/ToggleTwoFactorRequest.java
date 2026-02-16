package com.banking.system.auth.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.auth.application.dto.command.ToggleTwoFactorCommand;
import jakarta.validation.constraints.NotNull;

public record ToggleTwoFactorRequest(
        @NotNull(message = "enable field is required")
        Boolean enable
) {
    public ToggleTwoFactorCommand toCommand() {
        return new ToggleTwoFactorCommand(enable);
    }
}
