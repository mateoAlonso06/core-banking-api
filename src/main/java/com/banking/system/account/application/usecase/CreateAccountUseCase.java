package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.command.CreateAccountCommand;
import com.banking.system.account.application.dto.result.AccountResult;

import java.util.UUID;

public interface CreateAccountUseCase {
    AccountResult createAccount(CreateAccountCommand command, UUID userId);
}
