package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class AccountAccessDeniedException extends BusinessRuleException {
    public AccountAccessDeniedException(String message) {
        super(message);
    }
}