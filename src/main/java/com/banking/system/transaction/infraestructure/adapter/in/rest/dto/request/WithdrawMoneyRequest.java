package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawMoneyRequest(
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotNull UUID idempotencyKey
) {
    public WithdrawMoneyCommand toCommand() {
        return new WithdrawMoneyCommand(this.amount, this.currency, this.idempotencyKey);
    }
}
