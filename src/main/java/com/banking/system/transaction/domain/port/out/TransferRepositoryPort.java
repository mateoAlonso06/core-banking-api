package com.banking.system.transaction.domain.port.out;

import com.banking.system.transaction.domain.model.Transfer;

import java.util.List;
import java.util.UUID;

public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Transfer findById(UUID id);

    List<Transfer> findAll(int page, int size);

    void deleteById(UUID id);

    Transfer findByAccountId(UUID accountId);
}
