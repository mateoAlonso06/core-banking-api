package com.banking.system.customer.application.usecase;

import com.banking.system.customer.application.dto.command.UpdateCustomerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;

import java.util.UUID;

public interface UpdateCustomerUseCase {
    CustomerResult updateCustomer(UpdateCustomerCommand command, UUID userId);
}
