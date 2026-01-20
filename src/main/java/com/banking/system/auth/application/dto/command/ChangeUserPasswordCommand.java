package com.banking.system.auth.application.dto.command;

public record ChangeUserPasswordCommand (
        String oldPassword,
        String newPassword
) {
}
