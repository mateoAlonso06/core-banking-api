package com.banking.system.account.domain.exception;

import com.banking.system.common.domain.exception.InfrastructureException;

public class AliasGenerationFailedException extends InfrastructureException {
    public AliasGenerationFailedException(String message) {
        super(message);
    }
}
