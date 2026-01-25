package com.banking.system.transaction.application.dto.command;

import java.math.BigDecimal;

public record WithdrawMoneyCommand(
        BigDecimal amount,
        String currency
) {

}
