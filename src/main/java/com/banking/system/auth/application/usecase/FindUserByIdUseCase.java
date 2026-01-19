package com.banking.system.auth.application.usecase;

import com.banking.system.auth.application.dto.result.UserResult;

import java.util.UUID;

public interface FindUserByIdUseCase {
    UserResult findById(UUID userId);
}
