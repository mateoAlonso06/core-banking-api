package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.AccessDeniedException;

public class InvalidAccountOwnerException extends AccessDeniedException {
    public InvalidAccountOwnerException(String message) {
        super(message, "INVALID_ACCOUNT_OWNER");
    }
}
