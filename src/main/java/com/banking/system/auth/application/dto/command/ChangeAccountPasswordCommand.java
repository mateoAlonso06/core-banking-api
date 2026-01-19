package com.banking.system.auth.application.dto.command;

public record ChangeAccountPasswordCommand(
        String oldPassword,
        String newPassword
) {
}
