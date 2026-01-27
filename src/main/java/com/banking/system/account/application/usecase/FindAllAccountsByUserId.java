package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.result.AccountResult;

import java.util.List;
import java.util.UUID;

public interface FindAllAccountsByUserId {
    List<AccountResult> findAll(UUID userId);
}
