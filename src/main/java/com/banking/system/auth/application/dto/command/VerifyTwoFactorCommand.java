package com.banking.system.auth.application.dto.command;

public record VerifyTwoFactorCommand(
        String sessionToken,
        String code
) {
}
