package com.banking.system.auth.application.dto.command;

public record LoginCommand(
        String email,
        String password
) {
}
