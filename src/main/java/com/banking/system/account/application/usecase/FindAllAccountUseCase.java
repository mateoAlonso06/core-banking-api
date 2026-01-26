package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.result.AccountResult;
import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;

import java.util.List;

public interface FindAllAccountUseCase {
    PagedResult<AccountResult> findAll(PageRequest pageRequest);
}
