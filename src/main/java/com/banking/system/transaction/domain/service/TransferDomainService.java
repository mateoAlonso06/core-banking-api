package com.banking.system.transaction.domain.service;

import com.banking.system.account.domain.model.Account;
import com.banking.system.common.domain.Money;
import com.banking.system.transaction.domain.exception.SameAccountTransferException;
import com.banking.system.transaction.domain.exception.TransferCurrencyMismatchException;
import com.banking.system.transaction.domain.model.Description;
import org.springframework.stereotype.Component;

@Component
public class TransferDomainService {

    public void transfer(Account sourceAccount, Account targetAccount, Money amount, Description description, Money feeAmount) {
        validateDifferentAccounts(sourceAccount, targetAccount);
        validateSameCurrency(sourceAccount, targetAccount);

        if (feeAmount != null) {
            amount.add(feeAmount);
        }

        sourceAccount.debit(amount);
        targetAccount.credit(amount);
    }

    private void validateDifferentAccounts(Account sourceAccount, Account targetAccount) {
        if (sourceAccount.equals(targetAccount)) {
            throw new SameAccountTransferException("Source and target accounts must be different");
        }
    }

    private void validateSameCurrency(Account sourceAccount, Account targetAccount) {
        if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
            throw new TransferCurrencyMismatchException("Source and target account currencies do not match");
        }
    }
}
