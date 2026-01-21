package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class AccountNotActiveException extends BusinessRuleException {
    public AccountNotActiveException(String message) {
        super(message);
    }
}
