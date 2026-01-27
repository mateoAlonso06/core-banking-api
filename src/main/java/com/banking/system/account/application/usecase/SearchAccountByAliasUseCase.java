package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.result.AccountPublicResult;

public interface SearchAccountByAliasUseCase {
    AccountPublicResult searchByAlias(String alias);
}
