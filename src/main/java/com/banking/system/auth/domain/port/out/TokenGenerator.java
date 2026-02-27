package com.banking.system.auth.domain.port.out;

import java.util.UUID;

public interface TokenGenerator {
    String generateToken(UUID userId, String role);
}