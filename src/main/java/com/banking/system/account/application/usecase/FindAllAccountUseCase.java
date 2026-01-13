package com.banking.system.account.application.usecase;

import com.banking.system.account.application.dto.resut.AccountResult;

import java.util.List;

public interface FindAllAccountUseCase {
    List<AccountResult> findAll(int page, int size);
}
