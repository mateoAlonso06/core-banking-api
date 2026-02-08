package com.banking.system.auth.domain.port.out;

import com.banking.system.auth.domain.model.TwoFactorCode;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorCodeRepositoryPort {
    TwoFactorCode save(TwoFactorCode twoFactorCode);

    Optional<TwoFactorCode> findBySessionToken(String sessionToken);

    Optional<TwoFactorCode> findLatestByUserId(UUID userId);

    void deleteExpiredCodes();
}
