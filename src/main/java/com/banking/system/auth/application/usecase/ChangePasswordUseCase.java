package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.ChangeAccountPasswordCommand;

import java.util.UUID;

public interface ChangePasswordUseCase {
    void changePassword(UUID userId, ChangeAccountPasswordCommand command);
}
