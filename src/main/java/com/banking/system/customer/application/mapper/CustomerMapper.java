package com.banking.system.customer.application.mapper;

import com.banking.system.common.domain.Address;
import com.banking.system.common.domain.IdentityDocument;
import com.banking.system.common.domain.PersonName;
import com.banking.system.common.domain.Phone;
import com.banking.system.customer.application.dto.command.CreateCustomerCommand;
import com.banking.system.customer.domain.model.Customer;

public class CustomerMapper {
    public static Customer toDomain(CreateCustomerCommand command) {
        return Customer.createNewCustomer(
                command.userId(),
                new PersonName(command.firstName(), command.lastName()),
                new IdentityDocument(command.documentNumber(), command.documentType()),
                command.birthDate(),
                new Phone(command.phone()),
                new Address(command.address(), command.city(), command.country())
        );
    }
}
