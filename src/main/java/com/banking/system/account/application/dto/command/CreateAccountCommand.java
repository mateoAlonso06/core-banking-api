package com.banking.system.account.application.dto.command;

import com.banking.system.account.domain.model.AccountType;

import java.util.UUID;

public record CreateAccountCommand(
    UUID customerId,
    AccountType accountType,
    String currency
) {
}
