package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class TransferCurrencyMismatchException extends BusinessRuleException {
    public TransferCurrencyMismatchException(String message) {
        super(message);
    }
}
