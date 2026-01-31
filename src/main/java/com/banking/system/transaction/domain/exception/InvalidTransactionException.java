package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class InvalidTransactionException extends BusinessRuleException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
