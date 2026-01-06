package com.banking.system.customer.application.mapper;

import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.application.dto.command.UpdateCustommerCommand;
import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.domain.model.Customer;

import java.util.UUID;

public class CustomerMapper {
    public static CustomerResult toResult(Customer customer) {
        return new CustomerResult(
                customer.getId(),
                customer.getUserId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDocumentType(),
                customer.getDocumentNumber(),
                customer.getBirthDate(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getCountry()
        );
    }

    public static Customer toDomain(CreateCustomerCommand command) {
        return Customer.createNewCustomer(
                command.userId(),
                command.firstName(),
                command.lastName(),
                command.documentType(),
                command.documentNumber(),
                command.birthDate(),
                command.phone(),
                command.address(),
                command.city(),
                command.country()
        );
    }
}
