package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class SameAccountTransferException extends BusinessRuleException {
    public SameAccountTransferException(String message) {
        super(message, "SAME_ACCOUNT_TRANSFER");
    }
}
