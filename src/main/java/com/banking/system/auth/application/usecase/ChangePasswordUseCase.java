package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.ChangeUserPasswordCommand;

import java.util.UUID;

public interface ChangePasswordUseCase {
    void changePassword(UUID userId, ChangeUserPasswordCommand command);
}
