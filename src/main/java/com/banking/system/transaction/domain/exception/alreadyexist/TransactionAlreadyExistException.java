package com.banking.system.transaction.domain.exception.alreadyexist;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class TransactionAlreadyExistException extends ResourceAlreadyExistsException {
    public TransactionAlreadyExistException(String message) {
        super(message, "TRANSACTION_ALREADY_EXISTS");
    }
}
