package com.banking.system.common.domain;

import java.util.Objects;

public record PersonName(String firstName, String lastName
) {
    public PersonName {
        Objects.requireNonNull(firstName);
        Objects.requireNonNull(lastName);

        if (firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }

        if (lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }

        if (firstName.length() > 100 || lastName.length() > 100) {
            throw new IllegalArgumentException("First name and last name must be less than 101 characters.");
        }

        // Only accept letters (including international characters) for both names
        if (!firstName.matches("^[\\\\p{L}]+$")) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (!lastName.matches("^[\\\\p{L}]+$")) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
