package com.banking.system.customer.application.usecase;

import java.util.UUID;

public interface DeleteCustomerUseCase {
    void deleteCustomerById(UUID id);
}
