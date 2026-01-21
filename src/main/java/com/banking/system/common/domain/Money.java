package com.banking.system.common.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money represents a monetary value with a fixed currency.
 * It enforces rounding and currency consistency rules.
 */
@Getter
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal value;
    private final MoneyCurrency currency;

    private Money(BigDecimal value, MoneyCurrency currency) {
        if (value == null)
            throw new IllegalArgumentException("Amount cannot be null");
        if (currency == null)
            throw new IllegalArgumentException("Currency cannot be null");

        this.value = value.setScale(SCALE, RoundingMode.HALF_EVEN);
        this.currency = currency;
    }

    public static Money of(BigDecimal value, MoneyCurrency currency) {
        return new Money(value, currency);
    }

    public static Money zero(MoneyCurrency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.add(other.value), currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.value.subtract(other.value), currency);
    }

    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Currency mismatch");
    }
}
