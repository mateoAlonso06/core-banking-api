package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(UUID userId);
    int deleteExpiredOrRevoked();
}