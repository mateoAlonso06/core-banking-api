package com.banking.system.customer.domain.port.out;

import com.banking.system.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    List<Customer> getAll(int page, int size);

    Optional<Customer> getById(UUID id);

    Customer save(Customer customer);

    void update(Customer customer);

    void delete(UUID id);

    boolean existsByDocumentNumber(String documentNumber);
}
