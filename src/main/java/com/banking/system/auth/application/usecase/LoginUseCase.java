package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.LoginCommand;
import com.banking.system.auth.application.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand loginCommand);
}
