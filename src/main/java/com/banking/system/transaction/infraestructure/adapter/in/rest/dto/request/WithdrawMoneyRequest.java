package com.banking.system.transaction.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.transaction.application.dto.command.WithdrawMoneyCommand;

import java.math.BigDecimal;

public record WithdrawMoneyRequest(BigDecimal amount, String currency) {
    public WithdrawMoneyCommand toCommand() {
        return new WithdrawMoneyCommand(this.amount, this.currency);
    }
}
