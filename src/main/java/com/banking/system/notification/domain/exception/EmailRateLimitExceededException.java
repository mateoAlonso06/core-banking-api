package com.banking.system.notification.domain.exception;

public class EmailRateLimitExceededException extends RuntimeException {
    public EmailRateLimitExceededException(String message) {
        super(message);
    }
}