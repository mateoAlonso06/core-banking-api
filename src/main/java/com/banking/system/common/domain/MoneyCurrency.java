package com.banking.system.common.domain;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Value object that represents a restricted ISO-4217 currency.
 * <p>
 * This type wraps {@link Currency} to avoid using free-text strings across the domain.
 * It normalizes input (trim/uppercase) and fails fast on invalid or unsupported currency codes.
 * <p>
 * Application-level constraint: currently only ARS (Argentine Peso) and USD (US Dollar)
 * are allowed. Any other valid ISO-4217 code (e.g. EUR, BRL) will be rejected with
 * an {@link IllegalArgumentException}.
 */
public record MoneyCurrency(Currency value) {

    private static final Set<String> ALLOWED_CODES = Set.of("ARS", "USD");

    public MoneyCurrency {
        Objects.requireNonNull(value, "currency must not be null");

        if (!ALLOWED_CODES.contains(value.getCurrencyCode())) {
            throw new IllegalArgumentException("Currency not supported: " + value.getCurrencyCode());
        }
    }
    /**
     * Creates a {@link MoneyCurrency} from an ISO-4217 currency code.
     *
     * @throws IllegalArgumentException if the code is invalid or not supported
     */
    public static MoneyCurrency ofCode(String code) {
        Objects.requireNonNull(code, "currency code must not be null");
        String normalized = code.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_CODES.contains(normalized)) {
            throw new IllegalArgumentException("Currency not supported: " + normalized);
        }

        Currency currency = Currency.getInstance(normalized);
        return new MoneyCurrency(currency);
    }

    /**
     * Creates a {@link MoneyCurrency} from a {@link Currency} instance.
     */
    public static MoneyCurrency of(Currency currency) {
        return new MoneyCurrency(currency);
    }

    public String code() {
        return value.getCurrencyCode();
    }

    public Currency asJavaCurrency() {
        return value;
    }
}
