package com.banking.system.transaction.application.service;

import com.banking.system.common.domain.Money;
import com.banking.system.transaction.application.usecase.DepositUseCase;
import com.banking.system.transaction.application.usecase.TransferUseCase;
import com.banking.system.transaction.application.usecase.WithdrawUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements TransferUseCase, DepositUseCase, WithdrawUseCase {


    @Override
    public void deposit(UUID accountId, Money amount) {

    }

    @Override
    public void transfer(UUID fromAccountId, UUID toAccountId, Money amount) {

    }

    @Override
    public void withdraw(UUID accountId, Money amount) {

    }
}
