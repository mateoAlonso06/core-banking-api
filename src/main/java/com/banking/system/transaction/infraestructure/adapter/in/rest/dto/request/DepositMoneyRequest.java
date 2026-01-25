package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.transaction.application.dto.command.DepositMoneyCommand;

import java.math.BigDecimal;

public record DepositMoneyRequest(
        BigDecimal amount,
        String currency
) {
    public DepositMoneyCommand toCommand() {
        return new DepositMoneyCommand(this.amount, this.currency);
    }
}
