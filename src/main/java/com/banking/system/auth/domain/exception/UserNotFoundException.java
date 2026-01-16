package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
