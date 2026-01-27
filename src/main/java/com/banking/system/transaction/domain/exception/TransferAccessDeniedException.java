package com.banking.system.transaction.domain.exception;

import com.banking.system.common.domain.exception.AccessDeniedException;

public class TransferAccessDeniedException extends AccessDeniedException {
    public TransferAccessDeniedException(String message) {
        super(message);
    }
}
