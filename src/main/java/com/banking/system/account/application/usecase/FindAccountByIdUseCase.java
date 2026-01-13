package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.resut.AccountResult;

import java.util.UUID;

public interface FindAccountByIdUseCase {
    AccountResult findById(UUID accountId);
}
