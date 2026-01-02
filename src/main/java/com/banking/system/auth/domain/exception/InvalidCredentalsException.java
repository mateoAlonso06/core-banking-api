package com.banking.system.auth.domain.exception;

public class InvalidCredentalsException extends RuntimeException {
    public InvalidCredentalsException(String message) {
        super(message);
    }
}
