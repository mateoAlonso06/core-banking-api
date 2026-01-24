package com.banking.system.common;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class CurrencyMismatchException extends BusinessRuleException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
