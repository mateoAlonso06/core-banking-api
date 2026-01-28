package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.ResendVerificationCommand;

public interface ResendVerificationEmailUseCase {
    void resendVerificationEmail(ResendVerificationCommand command);
}