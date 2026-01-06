package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.command.UpdateCustommerCommand;

import java.util.UUID;

public interface UpdateCustomerUseCase {
    void updateCustomer(UUID id, UpdateCustommerCommand command);
}
