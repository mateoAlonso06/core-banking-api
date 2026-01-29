package com.banking.system.unit.common.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import com.banking.system.common.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    private static final MoneyCurrency ARS = MoneyCurrency.ofCode("ARS");
    private static final MoneyCurrency USD = MoneyCurrency.ofCode("USD");

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid money with positive amount")
        void shouldCreateValidMoneyWithPositiveAmount() {
            Money money = Money.of(new BigDecimal("100.00"), ARS);

            assertNotNull(money);
            assertEquals(new BigDecimal("100.00"), money.getValue());
            assertEquals(ARS, money.getCurrency());
        }

        @Test
        @DisplayName("Should create valid money with zero amount")
        void shouldCreateValidMoneyWithZeroAmount() {
            Money money = Money.of(BigDecimal.ZERO, ARS);

            assertEquals(new BigDecimal("0.00"), money.getValue());
        }

        @Test
        @DisplayName("Should create valid money with negative amount")
        void shouldCreateValidMoneyWithNegativeAmount() {
            Money money = Money.of(new BigDecimal("-50.00"), ARS);

            assertEquals(new BigDecimal("-50.00"), money.getValue());
        }

        @Test
        @DisplayName("Should create zero money using factory method")
        void shouldCreateZeroMoneyUsingFactoryMethod() {
            Money money = Money.zero(USD);

            assertEquals(new BigDecimal("0.00"), money.getValue());
            assertEquals(USD, money.getCurrency());
        }

        @Test
        @DisplayName("Should create money with different currencies")
        void shouldCreateMoneyWithDifferentCurrencies() {
            Money arsAmount = Money.of(new BigDecimal("1000.00"), ARS);
            Money usdAmount = Money.of(new BigDecimal("100.00"), USD);

            assertEquals(ARS, arsAmount.getCurrency());
            assertEquals(USD, usdAmount.getCurrency());
        }
    }

    @Nested
    @DisplayName("Scale and Rounding Tests")
    class ScaleAndRoundingTests {

        @Test
        @DisplayName("Should round to 2 decimal places using HALF_EVEN")
        void shouldRoundTo2DecimalPlacesUsingHalfEven() {
            Money money = Money.of(new BigDecimal("100.125"), ARS);

            assertEquals(new BigDecimal("100.12"), money.getValue());
        }

        @Test
        @DisplayName("Should round up on HALF_EVEN when preceding digit is odd")
        void shouldRoundUpOnHalfEvenWhenPrecedingDigitIsOdd() {
            Money money = Money.of(new BigDecimal("100.135"), ARS);

            assertEquals(new BigDecimal("100.14"), money.getValue());
        }

        @Test
        @DisplayName("Should add trailing zeros for whole numbers")
        void shouldAddTrailingZerosForWholeNumbers() {
            Money money = Money.of(new BigDecimal("100"), ARS);

            assertEquals(new BigDecimal("100.00"), money.getValue());
        }

        @Test
        @DisplayName("Should preserve scale for exact values")
        void shouldPreserveScaleForExactValues() {
            Money money = Money.of(new BigDecimal("99.99"), ARS);

            assertEquals(2, money.getValue().scale());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when value is null")
        void shouldThrowNpeWhenValueIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> Money.of(null, ARS)
            );
            assertEquals("Amount cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when currency is null")
        void shouldThrowNpeWhenCurrencyIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> Money.of(new BigDecimal("100"), null)
            );
            assertEquals("Currency cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when currency is null in zero factory")
        void shouldThrowNpeWhenCurrencyIsNullInZeroFactory() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> Money.zero(null)
            );
            assertEquals("Currency cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("add() Method Tests")
    class AddMethodTests {

        @Test
        @DisplayName("Should add two positive amounts")
        void shouldAddTwoPositiveAmounts() {
            Money money1 = Money.of(new BigDecimal("100.00"), ARS);
            Money money2 = Money.of(new BigDecimal("50.00"), ARS);

            Money result = money1.add(money2);

            assertEquals(new BigDecimal("150.00"), result.getValue());
            assertEquals(ARS, result.getCurrency());
        }

        @Test
        @DisplayName("Should add positive and negative amounts")
        void shouldAddPositiveAndNegativeAmounts() {
            Money money1 = Money.of(new BigDecimal("100.00"), ARS);
            Money money2 = Money.of(new BigDecimal("-30.00"), ARS);

            Money result = money1.add(money2);

            assertEquals(new BigDecimal("70.00"), result.getValue());
        }

        @Test
        @DisplayName("Should add to zero")
        void shouldAddToZero() {
            Money zero = Money.zero(ARS);
            Money money = Money.of(new BigDecimal("100.00"), ARS);

            Money result = zero.add(money);

            assertEquals(new BigDecimal("100.00"), result.getValue());
        }

        @Test
        @DisplayName("Should throw domain business rule exception when adding different currencies")
        void shouldThrowBusinessRuleExceptionWhenAddingDifferentCurrencies() {
            Money arsAmount = Money.of(new BigDecimal("100.00"), ARS);
            Money usdAmount = Money.of(new BigDecimal("50.00"), USD);

            CurrencyMismatchException exception = assertThrows(
                    CurrencyMismatchException.class,
                    () -> arsAmount.add(usdAmount)
            );
            assertEquals("Currency mismatch: " + arsAmount.getCurrency() + " vs " + usdAmount.getCurrency(), exception.getMessage());
        }

        @Test
        @DisplayName("Should return new instance on add (immutability)")
        void shouldReturnNewInstanceOnAdd() {
            Money original = Money.of(new BigDecimal("100.00"), ARS);
            Money toAdd = Money.of(new BigDecimal("50.00"), ARS);

            Money result = original.add(toAdd);

            assertNotSame(original, result);
            assertEquals(new BigDecimal("100.00"), original.getValue());
        }
    }

    @Nested
    @DisplayName("subtract() Method Tests")
    class SubtractMethodTests {

        @Test
        @DisplayName("Should subtract smaller from larger")
        void shouldSubtractSmallerFromLarger() {
            Money money1 = Money.of(new BigDecimal("100.00"), ARS);
            Money money2 = Money.of(new BigDecimal("30.00"), ARS);

            Money result = money1.subtract(money2);

            assertEquals(new BigDecimal("70.00"), result.getValue());
            assertEquals(ARS, result.getCurrency());
        }

        @Test
        @DisplayName("Should subtract larger from smaller (negative result)")
        void shouldSubtractLargerFromSmaller() {
            Money money1 = Money.of(new BigDecimal("30.00"), ARS);
            Money money2 = Money.of(new BigDecimal("100.00"), ARS);

            Money result = money1.subtract(money2);

            assertEquals(new BigDecimal("-70.00"), result.getValue());
        }

        @Test
        @DisplayName("Should subtract equal amounts to zero")
        void shouldSubtractEqualAmountsToZero() {
            Money money1 = Money.of(new BigDecimal("100.00"), ARS);
            Money money2 = Money.of(new BigDecimal("100.00"), ARS);

            Money result = money1.subtract(money2);

            assertEquals(new BigDecimal("0.00"), result.getValue());
        }

        @Test
        @DisplayName("Should subtract negative amount (effectively adding)")
        void shouldSubtractNegativeAmountEffectivelyAdding() {
            Money money1 = Money.of(new BigDecimal("100.00"), ARS);
            Money money2 = Money.of(new BigDecimal("-50.00"), ARS);

            Money result = money1.subtract(money2);

            assertEquals(new BigDecimal("150.00"), result.getValue());
        }

        @Test
        @DisplayName("Should throw business rule exception when subtracting different currencies")
        void shouldThrowBusinessRuleExceptionWhenSubtractingDifferentCurrencies() {
            Money arsAmount = Money.of(new BigDecimal("100.00"), ARS);
            Money usdAmount = Money.of(new BigDecimal("50.00"), USD);

            CurrencyMismatchException exception = assertThrows(
                    CurrencyMismatchException.class,
                    () -> arsAmount.subtract(usdAmount)
            );
            assertEquals("Currency mismatch: " + arsAmount.getCurrency() + " vs " + usdAmount.getCurrency(), exception.getMessage());
        }

        @Test
        @DisplayName("Should return new instance on subtract (immutability)")
        void shouldReturnNewInstanceOnSubtract() {
            Money original = Money.of(new BigDecimal("100.00"), ARS);
            Money toSubtract = Money.of(new BigDecimal("30.00"), ARS);

            Money result = original.subtract(toSubtract);

            assertNotSame(original, result);
            assertEquals(new BigDecimal("100.00"), original.getValue());
        }
    }

    @Nested
    @DisplayName("isNegative() Method Tests")
    class IsNegativeMethodTests {

        @Test
        @DisplayName("Should return true for negative amount")
        void shouldReturnTrueForNegativeAmount() {
            Money money = Money.of(new BigDecimal("-50.00"), ARS);

            assertTrue(money.isNegative());
        }

        @Test
        @DisplayName("Should return false for positive amount")
        void shouldReturnFalseForPositiveAmount() {
            Money money = Money.of(new BigDecimal("50.00"), ARS);

            assertFalse(money.isNegative());
        }

        @Test
        @DisplayName("Should return false for zero amount")
        void shouldReturnFalseForZeroAmount() {
            Money money = Money.zero(ARS);

            assertFalse(money.isNegative());
        }

        @Test
        @DisplayName("Should return true for small negative amount")
        void shouldReturnTrueForSmallNegativeAmount() {
            Money money = Money.of(new BigDecimal("-0.01"), ARS);

            assertTrue(money.isNegative());
        }
    }

    @Nested
    @DisplayName("isZero() Method Tests")
    class IsZeroMethodTests {

        @Test
        @DisplayName("Should return true for zero amount")
        void shouldReturnTrueForZeroAmount() {
            Money money = Money.zero(ARS);

            assertTrue(money.isZero());
        }

        @Test
        @DisplayName("Should return false for positive amount")
        void shouldReturnFalseForPositiveAmount() {
            Money money = Money.of(new BigDecimal("10.00"), ARS);

            assertFalse(money.isZero());
        }

        @Test
        @DisplayName("Should return false for negative amount")
        void shouldReturnFalseForNegativeAmount() {
            Money money = Money.of(new BigDecimal("-10.00"), ARS);

            assertFalse(money.isZero());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should not allow modification of value")
        void shouldNotAllowModificationOfValue() {
            Money money = Money.of(new BigDecimal("100.00"), ARS);
            BigDecimal originalValue = money.getValue();

            assertEquals(originalValue, money.getValue());
        }

        @Test
        @DisplayName("Should not allow modification of currency")
        void shouldNotAllowModificationOfCurrency() {
            Money money = Money.of(new BigDecimal("100.00"), ARS);
            MoneyCurrency originalCurrency = money.getCurrency();

            assertEquals(originalCurrency, money.getCurrency());
        }

        @Test
        @DisplayName("Add operation should not modify original")
        void addOperationShouldNotModifyOriginal() {
            Money original = Money.of(new BigDecimal("100.00"), ARS);
            Money toAdd = Money.of(new BigDecimal("50.00"), ARS);

            original.add(toAdd);

            assertEquals(new BigDecimal("100.00"), original.getValue());
        }

        @Test
        @DisplayName("Subtract operation should not modify original")
        void subtractOperationShouldNotModifyOriginal() {
            Money original = Money.of(new BigDecimal("100.00"), ARS);
            Money toSubtract = Money.of(new BigDecimal("30.00"), ARS);

            original.subtract(toSubtract);

            assertEquals(new BigDecimal("100.00"), original.getValue());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large amounts")
        void shouldHandleVeryLargeAmounts() {
            BigDecimal largeAmount = new BigDecimal("999999999999999.99");
            Money money = Money.of(largeAmount, ARS);

            assertEquals(largeAmount, money.getValue());
        }

        @Test
        @DisplayName("Should handle very small positive amounts")
        void shouldHandleVerySmallPositiveAmounts() {
            Money money = Money.of(new BigDecimal("0.01"), ARS);

            assertEquals(new BigDecimal("0.01"), money.getValue());
            assertFalse(money.isNegative());
        }

        @Test
        @DisplayName("Should chain multiple operations")
        void shouldChainMultipleOperations() {
            Money money = Money.of(new BigDecimal("100.00"), ARS);

            Money result = money
                    .add(Money.of(new BigDecimal("50.00"), ARS))
                    .subtract(Money.of(new BigDecimal("30.00"), ARS))
                    .add(Money.of(new BigDecimal("10.00"), ARS));

            assertEquals(new BigDecimal("130.00"), result.getValue());
        }

        @Test
        @DisplayName("Should handle precision in calculations")
        void shouldHandlePrecisionInCalculations() {
            Money money1 = Money.of(new BigDecimal("0.10"), ARS);
            Money money2 = Money.of(new BigDecimal("0.20"), ARS);

            Money result = money1.add(money2);

            assertEquals(new BigDecimal("0.30"), result.getValue());
        }
    }
}