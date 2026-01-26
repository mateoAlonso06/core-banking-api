package com.banking.system.common.domain.exception;

public class CurrencyMismatchException extends BusinessRuleException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
