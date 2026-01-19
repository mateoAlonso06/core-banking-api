package com.banking.system.auth.application.dto.result;

import java.util.UUID;

public record UserResult(UUID id, String email) {
}
