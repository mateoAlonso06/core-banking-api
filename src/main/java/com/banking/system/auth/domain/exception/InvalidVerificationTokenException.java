package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class InvalidVerificationTokenException extends BusinessRuleException {
    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}