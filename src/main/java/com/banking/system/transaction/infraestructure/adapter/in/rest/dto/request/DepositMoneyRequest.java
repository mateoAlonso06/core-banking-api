package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositMoneyRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotNull UUID idempotencyKey
) {
    public DepositMoneyCommand toCommand() {
        return new DepositMoneyCommand(this.amount, this.currency, this.idempotencyKey);
    }
}
