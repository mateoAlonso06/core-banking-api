package com.banking.system.transaction.domain.exception.notfound;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class TransactionNotFoundException extends ResourceNotFoundException {
    public TransactionNotFoundException(String message) {
        super(message, "TRANSACTION_NOT_FOUND");
    }
}
