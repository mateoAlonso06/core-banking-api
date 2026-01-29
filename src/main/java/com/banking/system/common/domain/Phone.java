package com.banking.system.common.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record Phone(String number) {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\+549\\d{10}$"); // +54 9 11 1234-5678
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^\\+54\\d{10}$"); // +54 11 1234-5678
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d{8,13}$"); // Fallback: 8-13 digits

    public Phone {
        Objects.requireNonNull(number);

        number = number.trim().replaceAll("[\\s()\\-]", "");

        if (number.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be blank");
        }

        // Validate '+' symbol usage
        long plusCount = number.chars().filter(ch -> ch == '+').count();
        if (plusCount > 1) {
            throw new IllegalArgumentException("'+' symbol must be at the start for international format");
        }
        if (plusCount == 1 && !number.startsWith("+")) {
            throw new IllegalArgumentException("'+' symbol must be at the start for international format");
        }

        if (number.length() < 8 || number.length() > 20) {
            throw new IllegalArgumentException("Phone number must be between 8 and 20 digits long");
        }

        if (!isValidFormat(number)) {
            throw new IllegalArgumentException("Invalid phone number format. Use international format (+549...) or local digits only.");
        }
    }

    /**
     * Validates phone number format.
     * Accepts:
     * - International format: +54 9 11 1234-5678 (mobile) or +54 11 1234-5678 (landline)
     * - Digits only: 1112345678 (10 digits for Argentina)
     */
    private static boolean isValidFormat(String number) {
        // If starts with +, validate as international format
        if (number.startsWith("+")) {
            return MOBILE_PATTERN.matcher(number).matches() ||
                    LANDLINE_PATTERN.matcher(number).matches();
        }

        // Otherwise, validate as digits only (local format)
        return DIGITS_ONLY_PATTERN.matcher(number).matches();
    }

    /**
     * Returns true if this is a mobile phone (starts with +549).
     */
    public boolean isMobile() {
        return number.startsWith("+549");
    }

    /**
     * Returns true if this is a landline phone (starts with +54 but not +549).
     */
    public boolean isLandline() {
        return number.startsWith("+54") && !number.startsWith("+549");
    }

    /**
     * Returns the phone number in a display-friendly format.
     * Example: +54 9 11 1234-5678
     */
    public String formatted() {
        if (!number.startsWith("+54")) {
            return number; // Local format, return as-is
        }

        // International format: +54 9 11 1234-5678 or +54 11 1234-5678
        if (isMobile()) {
            // +549 11 12345678 → +54 9 11 1234-5678
            return "+54 9 " + number.substring(4, 6) + " " +
                    number.substring(6, 10) + "-" + number.substring(10);
        } else {
            // +54 11 12345678 → +54 11 1234-5678
            return "+54 " + number.substring(3, 5) + " " +
                    number.substring(5, 9) + "-" + number.substring(9);
        }
    }

    /**
     * Returns a masked version for logging/display (shows only last 4 digits).
     * Example: ***-5678
     */
    public String masked() {
        return "***-" + number.substring(number.length() - 4);
    }
}
