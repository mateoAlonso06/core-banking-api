package com.banking.system.auth.application.dto.result;

import java.util.UUID;

public record RegisterResult(
    UUID id,
    String email
) {
}
