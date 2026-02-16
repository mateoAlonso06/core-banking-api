package com.banking.system.customer.infraestructure.adapter.in.rest.dto.request;

import com.banking.system.common.infraestructure.utils.SanitizeHtml;
import com.banking.system.customer.application.dto.command.UpdateCustomerCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @SanitizeHtml
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @SanitizeHtml
        String lastName,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        @SanitizeHtml
        String phone,

        @Size(max = 200, message = "Address must not exceed 200 characters")
        @SanitizeHtml
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        @SanitizeHtml
        String city,

        @Size(min = 2, max = 2, message = "Country must be a 2-character ISO code")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be uppercase ISO 3166-1 alpha-2 code")
        String country
) {
    public UpdateCustomerCommand toCommand() {
        return new UpdateCustomerCommand(firstName, lastName, phone, address, city, country);
    }
}
