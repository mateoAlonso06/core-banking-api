package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.RegisterCommand;
import com.banking.system.auth.application.dto.RegisterResult;

public interface RegisterUseCase {
    RegisterResult register(RegisterCommand registerUserRequest);
}
