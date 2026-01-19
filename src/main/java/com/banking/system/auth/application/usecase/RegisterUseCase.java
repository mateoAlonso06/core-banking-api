package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.RegisterCommand;
import com.banking.system.auth.application.dto.result.RegisterResult;

public interface RegisterUseCase {
    RegisterResult register(RegisterCommand registerUserRequest);
}
