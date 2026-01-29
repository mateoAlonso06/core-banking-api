package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class TransactionAlreadyExistException extends ResourceAlreadyExistsException {
    public TransactionAlreadyExistException(String message) {
        super(message);
    }
}
