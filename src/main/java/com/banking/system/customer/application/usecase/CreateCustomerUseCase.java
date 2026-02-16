package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;

public interface CreateCustomerUseCase {
    void createCustomer(CreateCustomerCommand command);
}
