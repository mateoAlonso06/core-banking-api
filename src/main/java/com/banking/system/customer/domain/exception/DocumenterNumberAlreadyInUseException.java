package com.banking.system.customer.domain.exception;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class DocumenterNumberAlreadyInUseException extends ResourceAlreadyExistsException {
    public DocumenterNumberAlreadyInUseException(String message) {
        super(message);
    }
}
