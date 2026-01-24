package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class KycNotApprovedException extends BusinessRuleException {
    public KycNotApprovedException(String message) {
        super(message);
    }
}
