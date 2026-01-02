package com.banking.system.auth.application.dto;

public record LoginCommand(
        String email,
        String password
) {
}
