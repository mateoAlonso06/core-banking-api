package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.ToggleTwoFactorCommand;
import com.banking.system.auth.application.dto.result.TwoFactorStatusResult;

import java.util.UUID;

public interface ToggleTwoFactorUseCase {
    TwoFactorStatusResult toggle(UUID userId, ToggleTwoFactorCommand command);
}
