package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.VerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepositoryPort {
    VerificationToken save(VerificationToken token);
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findLatestByUserId(UUID userId);
}