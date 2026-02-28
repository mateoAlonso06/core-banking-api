package com.banking.system.customer.domain.exception;

import com.banking.system.common.domain.exception.BusinessRuleException;

public class KycIsAlreadyProccedException extends BusinessRuleException {
    public KycIsAlreadyProccedException(String message) {
        super(message, "KYC_ALREADY_PROCESSED");
    }
}
