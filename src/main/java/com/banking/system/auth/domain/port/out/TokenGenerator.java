package com.banking.system.auth.domain.port.out;

import java.util.Set;
import java.util.UUID;

public interface TokenGenerator {
    String generateToken(UUID userId, String email, String role, Set<String> permissions);
}
