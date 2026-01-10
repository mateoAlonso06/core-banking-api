package com.banking.system.account.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;

import java.math.BigDecimal;

public class AccountLimits {
    public static final Money DEFAULT_DAILY_TRANSFER_LIMIT = Money.of(new BigDecimal("10000.00"), MoneyCurrency.ofCode("ARS"));
    public static final Money DEFAULT_MONTHLY_WITHDRAWAL_LIMIT = Money.of(new BigDecimal("50000.00"), MoneyCurrency.ofCode("ARS"));
}
