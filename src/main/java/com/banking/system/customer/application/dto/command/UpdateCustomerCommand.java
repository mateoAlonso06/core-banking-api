package com.banking.system.customer.application.dto.command;

public record UpdateCustomerCommand(
        String firstName,
        String lastName,
        String phone,
        String address,
        String city,
        String country
) {
}
