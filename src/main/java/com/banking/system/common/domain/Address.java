package com.banking.system.common.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record Address(
        String address,
        String city,
        String country
) {

    // ISO 3166-1 alpha-2 country codes
    // Using Java's built-in Locale for validation instead of hardcoding all 249 countries
    private static final Set<String> ISO_COUNTRY_CODES = Set.of(Locale.getISOCountries());

    /**
     * Compact constructor that validates and normalizes address data.
     * Country code must comply with ISO 3166-1 alpha-2 standard (2-letter codes).
     * Examples: AR (Argentina), US (United States), BR (Brazil), etc.
     */
    public Address {
        // Step 1: Null checks
        Objects.requireNonNull(address, "Address cannot be null");
        Objects.requireNonNull(city, "City cannot be null");
        Objects.requireNonNull(country, "Country code cannot be null");

        // Step 2: Normalize inputs (trim whitespace, uppercase for country code)
        address = address.trim();
        city = city.trim();
        country = country.trim().toUpperCase();

        // Step 3: Blank checks (after trimming)
        if (address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be blank");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("City cannot be blank");
        }
        if (country.isBlank()) {
            throw new IllegalArgumentException("Country code cannot be blank");
        }

        // Step 4: Validate country code format (must be exactly 2 characters)
        if (country.length() != 2) {
            throw new IllegalArgumentException(
                    "Country code must be exactly 2 characters (ISO 3166-1 alpha-2), got: " + country
            );
        }

        // Step 5: Validate country code is alphabetic
        if (!country.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException(
                    "Country code must contain only letters (ISO 3166-1 alpha-2), got: " + country
            );
        }

        // Step 6: Validate against ISO 3166-1 alpha-2 standard
        if (!ISO_COUNTRY_CODES.contains(country)) {
            throw new IllegalArgumentException(
                    "Invalid country code: '" + country + "'. Must be a valid ISO 3166-1 alpha-2 code (e.g., AR, US, BR)"
            );
        }

        // Step 7: Validate address and city length constraints
        if (address.length() < 5 || address.length() > 200) {
            throw new IllegalArgumentException(
                    "Address must be between 5 and 200 characters, got: " + address.length()
            );
        }
        if (city.length() < 2 || city.length() > 100) {
            throw new IllegalArgumentException(
                    "City must be between 2 and 100 characters, got: " + city.length()
            );
        }
    }

    /**
     * Returns the full country name for the ISO code.
     * Example: "AR" -> "Argentina"
     *
     * @return localized country name in English
     */
    public String countryName() {
        return new Locale.Builder()
                .setRegion(country)
                .build()
                .getDisplayCountry(Locale.ENGLISH);
    }

    /**
     * Returns a formatted address string for display purposes.
     *
     * @return formatted address (e.g., "Street 123, Buenos Aires, Argentina (AR)")
     */
    public String fullAddress() {
        return address + ", " + city + ", " + countryName() + " (" + country + ")";
    }
}
