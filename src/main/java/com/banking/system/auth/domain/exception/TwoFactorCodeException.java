package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class TwoFactorCodeException extends BusinessRuleException {
    public TwoFactorCodeException(String message) {
        super(message);
    }
}
