package com.banking.system.account.domain.exception;

public class AliasGenerationFailedException extends RuntimeException {
    public AliasGenerationFailedException(String message) {
        super(message);
    }
}
