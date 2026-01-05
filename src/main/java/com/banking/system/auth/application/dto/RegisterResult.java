package com.banking.system.auth.application.dto;

import java.util.UUID;

public record RegisterResult(
    UUID id,
    String email
) {
}
