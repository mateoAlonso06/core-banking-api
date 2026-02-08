package com.banking.system.auth.application.dto.command;

public record ToggleTwoFactorCommand(
        boolean enable
) {
}
