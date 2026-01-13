package com.banking.system.account.domain.exception;

public class InvalidAccountOwnerException extends RuntimeException {
    public InvalidAccountOwnerException(String message) {
        super(message);
    }
}
