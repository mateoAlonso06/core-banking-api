package com.banking.system.customer.domain.exception;

import com.banking.system.common.domain.exception.ResourceNotFoundException;

public class CustomerNotFoundException extends ResourceNotFoundException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
