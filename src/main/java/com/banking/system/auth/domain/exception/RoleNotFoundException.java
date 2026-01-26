package com.banking.system.auth.domain.exception;

import com.banking.system.common.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
