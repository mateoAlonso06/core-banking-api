package com.banking.system.transaction.domain.port.out;

import com.banking.system.transaction.domain.model.Transfer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findById(UUID id);

    List<Transfer> findAll(int page, int size);

    void deleteById(UUID id);

    Optional<Transfer> findByAccountId(UUID accountId);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
