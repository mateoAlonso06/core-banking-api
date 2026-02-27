package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.result.LoginResult;

public interface RefreshTokenUseCase {
    LoginResult refresh(String refreshToken);
}