package com.banking.system.transaction.infraestructure.adapter.out.mapper;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.transaction.domain.model.Description;
import com.banking.system.transaction.domain.model.IdempotencyKey;
import com.banking.system.transaction.domain.model.ReferenceNumber;
import com.banking.system.transaction.domain.model.Transaction;
import com.banking.system.transaction.infraestructure.adapter.out.persistence.entity.TransactionJpaEntity;

public class TransactionJpaEntityMapper {
    public static Transaction toDomainEntity(TransactionJpaEntity entity) {
        IdempotencyKey idempotencyKey = entity.getIdempotencyKey() != null
                ? IdempotencyKey.from(entity.getIdempotencyKey())
                : null;

        return Transaction.reconstitute(
                entity.getId(),
                entity.getAccountId(),
                entity.getTransactionType(),
                Money.of(entity.getAmount(), MoneyCurrency.ofCode(entity.getCurrency())),
                Money.of(entity.getBalanceAfter(), MoneyCurrency.ofCode(entity.getCurrency())),
                new Description(entity.getDescription()),
                new ReferenceNumber(entity.getReferenceNumber()),
                idempotencyKey,
                entity.getStatus(),
                entity.getExecutedAt()
        );
    }
}
