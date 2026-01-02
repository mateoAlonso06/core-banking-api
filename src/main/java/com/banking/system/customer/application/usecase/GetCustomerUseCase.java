package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.domain.model.Customer;

import java.util.List;
import java.util.UUID;

public interface GetCustomerUseCase {
    CustomerResult getCustomerById(UUID customerId);

    List<CustomerResult> getAll(int page, int size);
}
