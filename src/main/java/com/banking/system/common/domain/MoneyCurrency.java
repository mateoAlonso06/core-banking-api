package com.banking.system.common.domain;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Value object that represents an ISO-4217 currency.
 * <p>
 * This type wraps {@link Currency} to avoid using free-text strings across the domain.
 * It normalizes input (trim/uppercase) and fails fast on invalid currency codes.
 */
public record MoneyCurrency(Currency value) {

    public MoneyCurrency {
        Objects.requireNonNull(value, "currency must not be null");
    }

    /**
     * Creates a {@link MoneyCurrency} from an ISO-4217 currency code (e.g. "USD", "ARS").
     * <p>
     * Input is normalized with {@code trim()} and {@code toUpperCase(Locale.ROOT)}.
     *
     * @throws NullPointerException     if {@code code} is null
     * @throws IllegalArgumentException if {@code code} is not a valid ISO-4217 code
     */
    public static MoneyCurrency ofCode(String code) {
        Objects.requireNonNull(code, "currency code must not be null");
        Currency currency = Currency.getInstance(code.trim().toUpperCase(Locale.ROOT));
        return new MoneyCurrency(currency);
    }

    /**
     * Creates a {@link MoneyCurrency} from a {@link Currency} instance.
     */
    public static MoneyCurrency of(Currency currency) {
        return new MoneyCurrency(currency);
    }

    /**
     * @return the ISO-4217 currency code (e.g. "USD").
     */
    public String code() {
        return value.getCurrencyCode();
    }

    /**
     * @return the wrapped {@link Currency}.
     */
    public Currency asJavaCurrency() {
        return value;
    }
}

