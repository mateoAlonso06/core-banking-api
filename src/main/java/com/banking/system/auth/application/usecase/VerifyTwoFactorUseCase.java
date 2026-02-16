package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.VerifyTwoFactorCommand;
import com.banking.system.auth.application.dto.result.LoginResult;

public interface VerifyTwoFactorUseCase {
    LoginResult verify(VerifyTwoFactorCommand command);
}
