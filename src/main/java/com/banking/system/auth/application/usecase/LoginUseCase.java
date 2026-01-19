package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.command.LoginCommand;
import com.banking.system.auth.application.dto.result.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand loginCommand);
}
