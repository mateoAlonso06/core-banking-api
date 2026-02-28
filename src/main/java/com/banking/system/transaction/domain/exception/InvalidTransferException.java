package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

/*
    * Exception thrown when a transfer operation is invalid.
 */
public class InvalidTransferException extends BusinessRuleException {
    public InvalidTransferException(String message) {
        super(message, "INVALID_TRANSFER");
    }
}
