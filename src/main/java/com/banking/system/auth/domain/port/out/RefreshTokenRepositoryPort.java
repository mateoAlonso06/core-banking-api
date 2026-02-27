package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
}