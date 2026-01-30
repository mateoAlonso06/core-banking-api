package com.banking.system.common.domain;

import java.util.Objects;

public record PersonName(String firstName, String lastName
) {
    public PersonName {
        Objects.requireNonNull(firstName);
        Objects.requireNonNull(lastName);

        // Trim whitespace from both names
        firstName = firstName.trim();
        lastName = lastName.trim();

        if (firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }

        if (lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }

        if (firstName.length() > 100 || lastName.length() > 100) {
            throw new IllegalArgumentException("First name and last name must be less than 101 characters.");
        }

        // Reject invisible or problematic characters (Zero-width spaces, Hangul Fillers, etc.)
        if (containsInvisibleCharacters(firstName)) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (containsInvisibleCharacters(lastName)) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }

        // Only accept letters (including international characters) for both names
        if (!firstName.matches("^[\\p{L}]+$")) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (!lastName.matches("^[\\p{L}]+$")) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }
    }

    private static boolean containsInvisibleCharacters(String text) {
        // Check for invisible/problematic characters:
        // U+3164: Hangul Filler (ㅤ)
        // U+200B: Zero-width space
        // U+200C: Zero-width non-joiner
        // U+200D: Zero-width joiner
        // U+FEFF: Zero-width no-break space
        // U+180E: Mongolian vowel separator
        return text.contains("\u3164") ||
               text.contains("\u200B") ||
               text.contains("\u200C") ||
               text.contains("\u200D") ||
               text.contains("\uFEFF") ||
               text.contains("\u180E");
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
