package com.banking.system.transaction.domain.port.out;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.transaction.domain.model.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findById(UUID id);

    PagedResult<Transfer> getALlTransactionsByAccountId(PageRequest request, UUID accountId, UUID userId);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
