package com.banking.system.common.domain;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public record IdentityDocument(
        String number,
        String type
) {
    // Valid document types accepted by Argentine banks
    private static final Set<String> VALID_TYPES = Set.of(
            "DNI",      // National Identity Document (Argentina)
            "PASSPORT"  // International passport
    );

    // Argentine DNI format: 7-8 numeric digits (old DNI: 7 digits, new DNI: 8 digits)
    // Examples: 12345678, 1234567
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,8}$");

    // Argentine passport format: 3 letters + 6 digits (new format since 2012)
    // Examples: AAA123456, ABC987654
    // Also accepts old international format: 2 letters + 6-9 digits
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z]{2,3}\\d{6,9}$");

    /**
     * Compact constructor that validates and normalizes identity document data.
     * Validation order is critical: normalize first, then validate format.
     */
    public IdentityDocument {
        // Step 1: Null checks (before any operation)
        Objects.requireNonNull(number, "Identity document number cannot be null");
        Objects.requireNonNull(type, "Identity document type cannot be null");

        // Step 2: Normalize inputs (trim whitespace, remove separators, uppercase)
        number = number.trim().replaceAll("[\\s.-]", "").toUpperCase();
        type = type.trim().toUpperCase();

        // Step 3: Blank checks (after trimming)
        if (number.isBlank()) {
            throw new IllegalArgumentException("Identity document number cannot be blank");
        }
        if (type.isBlank()) {
            throw new IllegalArgumentException("Identity document type cannot be blank");
        }

        // Step 4: Validate document type against whitelist
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "Invalid identity document type: '" + type + "'. Allowed types: " + VALID_TYPES
            );
        }

        // Step 5: Validate length constraints
        if (number.length() < 5 || number.length() > 20) {
            throw new IllegalArgumentException(
                    "Identity document number must be between 5 and 20 characters, got: " + number.length()
            );
        }

        // Step 6: Validate format specific to document type
        validateFormat(number, type);

        // Step 7: Validate allowed characters (alphanumeric only, no special chars)
        if (!number.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException(
                    "Identity document number can only contain alphanumeric characters"
            );
        }
    }

    /**
     * Validates document number format based on document type.
     * Uses precompiled Pattern objects for performance (compiled once, reused many times).
     *
     * @param number normalized document number (already trimmed and uppercased)
     * @param type   validated document type
     * @throws IllegalArgumentException if format doesn't match expected pattern
     */
    private static void validateFormat(String number, String type) {
        switch (type) {
            case "DNI" -> {
                // Argentine DNI must be purely numeric, 7-8 digits
                // Old format (before 1990s): 7 digits
                // New format (current): 8 digits
                if (!DNI_PATTERN.matcher(number).matches()) {
                    throw new IllegalArgumentException(
                            "Invalid DNI format. Expected 7-8 numeric digits, got: " + number
                    );
                }
            }
            case "PASSPORT" -> {
                // Argentine passports since 2012: 3 letters + 6 digits (AAA123456)
                // Also accepts international format: 2 letters + 6-9 digits
                if (!PASSPORT_PATTERN.matcher(number).matches()) {
                    throw new IllegalArgumentException(
                            "Invalid PASSPORT format. Expected 2-3 letters + 6-9 digits, got: " + number
                    );
                }
            }
        }
    }

    /**
     * Returns a masked version of the document number for logging/auditing purposes.
     * Protects sensitive information by showing only first 2 and last 2 characters.
     * <p>
     * Examples:
     * - "12345678" → "12***78"
     * - "ABC123456" → "AB***56"
     * - "123" → "****" (too short to partially reveal)
     *
     * @return masked document number
     */
    public String maskedNumber() {
        if (number.length() <= 4) {
            return "****";
        }
        return number.substring(0, 2) + "***" + number.substring(number.length() - 2);
    }
}
