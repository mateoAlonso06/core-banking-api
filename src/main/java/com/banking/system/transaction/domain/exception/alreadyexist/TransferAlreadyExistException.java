package com.banking.system.transaction.domain.exception.alreadyexist;

import com.banking.system.common.domain.exception.ResourceAlreadyExistsException;

public class TransferAlreadyExistException extends ResourceAlreadyExistsException {
    public TransferAlreadyExistException(String message) {
        super(message);
    }
}
