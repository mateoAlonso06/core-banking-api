package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class TransactionNotFoundException extends ResourceNotFoundException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
