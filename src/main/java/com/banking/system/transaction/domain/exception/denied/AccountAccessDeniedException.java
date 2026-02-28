package com.banking.system.transaction.domain.exception.denied;

import com.banking.system.common.domain.exception.AccessDeniedException;

public class AccountAccessDeniedException extends AccessDeniedException {
    public AccountAccessDeniedException(String message) {
        super(message, "ACCOUNT_ACCESS_DENIED");
    }
}