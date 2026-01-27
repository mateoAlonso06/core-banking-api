package com.banking.system.customer.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.customer.application.dto.command.UpdateCustomerCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(min = 2, max = 2, message = "Country must be a 2-character ISO code")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be uppercase ISO 3166-1 alpha-2 code")
        String country
) {
    public UpdateCustomerCommand toCommand() {
        if ((firstName != null) != (lastName != null)) {
            throw new IllegalArgumentException("firstName and lastName must be provided together");
        }
        if (address != null || city != null || country != null) {
            if (address == null || city == null || country == null) {
                throw new IllegalArgumentException("address, city and country must be provided together");
            }
        }
        return new UpdateCustomerCommand(firstName, lastName, phone, address, city, country);
    }
}
