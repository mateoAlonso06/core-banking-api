package com.banking.system.customer.application.dto.result;

import com.banking.system.customer.domain.model.Customer;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for Customer HTTP responses.
 * Uses flat structure (decomposed Value Objects) for easier frontend consumption.
 */
public record CustomerResult(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String address,
        String city,
        String country
) {
    /**
     * Factory method to create CustomerResult from Customer domain entity.
     * Decomposes Value Objects into primitive fields for HTTP response.
     *
     * @param customer domain entity with Value Objects
     * @return flat DTO ready for JSON serialization
     */
    public static CustomerResult fromDomain(Customer customer) {
        return new CustomerResult(
                customer.getId(),
                customer.getUserId(),
                customer.getPersonName().firstName(),
                customer.getPersonName().lastName(),
                customer.getIdentityDocument().type(),
                customer.getIdentityDocument().number(),
                customer.getBirthDate(),
                customer.getPhone().number(),
                customer.getAddress().address(),
                customer.getAddress().city(),
                customer.getAddress().country()
        );
    }
}
