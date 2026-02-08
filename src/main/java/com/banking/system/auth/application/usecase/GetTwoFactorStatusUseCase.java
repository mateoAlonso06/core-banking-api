package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.result.TwoFactorStatusResult;

import java.util.UUID;

public interface GetTwoFactorStatusUseCase {
    TwoFactorStatusResult getStatus(UUID userId);
}
