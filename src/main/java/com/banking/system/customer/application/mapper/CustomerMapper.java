package com.banking.system.customer.application.mapper;

import com.banking.system.customer.application.dto.result.CustomerResult;
import com.banking.system.customer.domain.model.Customer;

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
                customer.getCountry(),
                customer.getKycStatus().name(),
                customer.getRiskLevel().name()
        );
    }
}
