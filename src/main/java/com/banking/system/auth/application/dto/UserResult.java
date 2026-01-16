package com.banking.system.auth.application.dto;

import java.util.UUID;

public record UserResult(UUID id, String email) {
}
