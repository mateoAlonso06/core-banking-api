package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.result.CustomerResult;

import java.util.UUID;

public interface GetCustomerUseCase {
    CustomerResult getCustomerById(UUID customerId);
    CustomerResult getCustomerByUserId(UUID userId);
}
