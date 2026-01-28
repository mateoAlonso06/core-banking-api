package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.VerifyEmailCommand;

public interface VerifyEmailUseCase {
    void verifyEmail(VerifyEmailCommand command);
}