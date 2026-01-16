package com.banking.system.account.domain.model;

import java.math.BigDecimal;

public final class AccountLimits {
    public static final BigDecimal DEFAULT_DAILY_TRANSFER = new BigDecimal("10000.00");
    public static final BigDecimal DEFAULT_MONTHLY_TRANSFER = new BigDecimal("50000.00");

    private AccountLimits() {}
}
