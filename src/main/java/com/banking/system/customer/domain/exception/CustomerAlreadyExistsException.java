package com.banking.system.customer.domain.exception;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class CustomerAlreadyExistsException extends ResourceAlreadyExistsException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
