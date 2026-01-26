package com.banking.system.customer.domain.port.out;

import com.banking.system.common.domain.PageRequest;
import com.banking.system.common.domain.dto.PagedResult;
import com.banking.system.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {
    PagedResult<Customer> findAll(PageRequest pageRequest);

    Optional<Customer> findById(UUID id);

    Customer save(Customer customer);

    void delete(UUID id);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsById(UUID id);

    boolean existsByUserId(UUID uuid);

    Optional<Customer> findByUserId(UUID uuid);
}
