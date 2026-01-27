package com.banking.system.account.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.account.application.dto.command.CreateAccountCommand;
import com.banking.system.account.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull(message = "Account type is required")
        AccountType accountType,
        @NotBlank(message = "Currency is required")
        String currency
) {

    public CreateAccountCommand toCommand() {
        return new CreateAccountCommand(
                this.accountType,
                this.currency
        );
    }
}
