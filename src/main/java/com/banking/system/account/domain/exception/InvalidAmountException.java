package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class InvalidAmountException extends BusinessRuleException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
