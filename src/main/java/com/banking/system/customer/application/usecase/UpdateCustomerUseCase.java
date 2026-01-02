package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.command.UpdateCustommerCommand;

public interface UpdateCustomerUseCase {
    void updateCustomer(UpdateCustommerCommand command);
}
