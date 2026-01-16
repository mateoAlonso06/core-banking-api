package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class InvalidAccountOwnerException extends BusinessRuleException {
    public InvalidAccountOwnerException(String message) {
        super(message);
    }
}
