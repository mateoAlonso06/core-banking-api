package com.banking.system.auth.application.usecase;

public interface LogoutUseCase {
    void logout(String refreshToken);
}