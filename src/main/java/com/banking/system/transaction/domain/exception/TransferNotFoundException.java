package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class TransferNotFoundException extends ResourceNotFoundException {
    public TransferNotFoundException(String message) {
        super(message);
    }
}
