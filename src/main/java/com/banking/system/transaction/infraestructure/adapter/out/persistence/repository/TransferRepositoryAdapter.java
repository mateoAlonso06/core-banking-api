package com.banking.system.transaction.infraestructure.adapter.out.persistence.repository;

import com.banking.system.transaction.domain.model.Transfer;
import com.banking.system.transaction.domain.port.out.TransferRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepositoryPort {
    @Override
    public Transfer save(Transfer transfer) {
        return null;
    }

    @Override
    public Transfer findById(UUID id) {
        return null;
    }

    @Override
    public List<Transfer> findAll(int page, int size) {
        return List.of();
    }

    @Override
    public void deleteById(UUID id) {

    }

    @Override
    public Transfer findByAccountId(UUID accountId) {
        return null;
    }
}
