package com.banking.system.unit.common.model.vo;

import com.banking.system.common.domain.MoneyCurrency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyCurrency Value Object Tests")
class MoneyCurrencyTest {

    @Nested
    @DisplayName("Valid Creation Tests")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create valid currency from code")
        void shouldCreateValidCurrencyFromCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertNotNull(currency);
            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should create valid ARS currency")
        void shouldCreateValidArsCurrency() {
            MoneyCurrency currency = MoneyCurrency.ofCode("ARS");

            assertEquals("ARS", currency.code());
        }

        @Test
        @DisplayName("Should create valid EUR currency")
        void shouldCreateValidEurCurrency() {
            MoneyCurrency currency = MoneyCurrency.ofCode("EUR");

            assertEquals("EUR", currency.code());
        }

        @Test
        @DisplayName("Should create currency from Currency instance")
        void shouldCreateCurrencyFromCurrencyInstance() {
            Currency javaCurrency = Currency.getInstance("USD");
            MoneyCurrency currency = MoneyCurrency.of(javaCurrency);

            assertEquals("USD", currency.code());
            assertEquals(javaCurrency, currency.value());
        }

        @Test
        @DisplayName("Should normalize code to uppercase")
        void shouldNormalizeCodeToUppercase() {
            MoneyCurrency currency = MoneyCurrency.ofCode("usd");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should trim whitespace from code")
        void shouldTrimWhitespaceFromCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("  USD  ");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should handle mixed case code")
        void shouldHandleMixedCaseCode() {
            MoneyCurrency currency = MoneyCurrency.ofCode("UsD");

            assertEquals("USD", currency.code());
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NPE when code is null")
        void shouldThrowNpeWhenCodeIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> MoneyCurrency.ofCode(null)
            );
            assertEquals("currency code must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when Currency instance is null")
        void shouldThrowNpeWhenCurrencyInstanceIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> MoneyCurrency.of(null)
            );
            assertEquals("currency must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NPE when value is null in constructor")
        void shouldThrowNpeWhenValueIsNullInConstructor() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new MoneyCurrency(null)
            );
            assertEquals("currency must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Invalid Code Validation Tests")
    class InvalidCodeValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"AAA", "ZZZ", "QQQ", "123", "INVALID"})
        @DisplayName("Should throw IAE for invalid currency codes")
        void shouldThrowIaeForInvalidCurrencyCodes(String invalidCode) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode(invalidCode)
            );
        }

        @Test
        @DisplayName("Should throw IAE for empty code")
        void shouldThrowIaeForEmptyCode() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("")
            );
        }

        @Test
        @DisplayName("Should throw IAE for blank code")
        void shouldThrowIaeForBlankCode() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("   ")
            );
        }

        @Test
        @DisplayName("Should throw IAE for code with special characters")
        void shouldThrowIaeForCodeWithSpecialCharacters() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MoneyCurrency.ofCode("US$")
            );
        }
    }

    @Nested
    @DisplayName("code() Method Tests")
    class CodeMethodTests {

        @Test
        @DisplayName("Should return correct code for USD")
        void shouldReturnCorrectCodeForUsd() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertEquals("USD", currency.code());
        }

        @Test
        @DisplayName("Should return correct code for ARS")
        void shouldReturnCorrectCodeForArs() {
            MoneyCurrency currency = MoneyCurrency.ofCode("ARS");

            assertEquals("ARS", currency.code());
        }

        @Test
        @DisplayName("Should return correct code for BRL")
        void shouldReturnCorrectCodeForBrl() {
            MoneyCurrency currency = MoneyCurrency.ofCode("BRL");

            assertEquals("BRL", currency.code());
        }
    }

    @Nested
    @DisplayName("asJavaCurrency() Method Tests")
    class AsJavaCurrencyMethodTests {

        @Test
        @DisplayName("Should return Java Currency instance")
        void shouldReturnJavaCurrencyInstance() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            Currency javaCurrency = currency.asJavaCurrency();

            assertNotNull(javaCurrency);
            assertEquals("USD", javaCurrency.getCurrencyCode());
        }

        @Test
        @DisplayName("Should return same instance as value()")
        void shouldReturnSameInstanceAsValue() {
            MoneyCurrency currency = MoneyCurrency.ofCode("EUR");

            assertSame(currency.value(), currency.asJavaCurrency());
        }
    }

    @Nested
    @DisplayName("value() Method Tests")
    class ValueMethodTests {

        @Test
        @DisplayName("Should return wrapped Currency instance")
        void shouldReturnWrappedCurrencyInstance() {
            Currency javaCurrency = Currency.getInstance("USD");
            MoneyCurrency currency = MoneyCurrency.of(javaCurrency);

            assertEquals(javaCurrency, currency.value());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode (Record) Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when currencies are equal")
        void shouldBeEqualWhenCurrenciesAreEqual() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.ofCode("USD");

            assertEquals(currency1, currency2);
            assertEquals(currency2, currency1);
        }

        @Test
        @DisplayName("Should be equal when created from same code with different case")
        void shouldBeEqualWhenCreatedFromSameCodeWithDifferentCase() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.ofCode("usd");

            assertEquals(currency1, currency2);
        }

        @Test
        @DisplayName("Should not be equal when currencies differ")
        void shouldNotBeEqualWhenCurrenciesDiffer() {
            MoneyCurrency usd = MoneyCurrency.ofCode("USD");
            MoneyCurrency eur = MoneyCurrency.ofCode("EUR");

            assertNotEquals(usd, eur);
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.ofCode("USD");

            assertEquals(currency1.hashCode(), currency2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertNotEquals(null, currency);
        }

        @Test
        @DisplayName("Should be equal to itself (reflexivity)")
        void shouldBeEqualToItself() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            assertEquals(currency, currency);
        }

        @Test
        @DisplayName("Should be equal when created from Currency instance")
        void shouldBeEqualWhenCreatedFromCurrencyInstance() {
            MoneyCurrency currency1 = MoneyCurrency.ofCode("USD");
            MoneyCurrency currency2 = MoneyCurrency.of(Currency.getInstance("USD"));

            assertEquals(currency1, currency2);
        }
    }

    @Nested
    @DisplayName("Immutability (Record) Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - value cannot be changed")
        void shouldBeImmutableValueCannotBeChanged() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");
            Currency originalValue = currency.value();

            assertEquals(originalValue, currency.value());
            assertEquals("USD", currency.code());
        }
    }

    @Nested
    @DisplayName("toString() Method (Record) Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            MoneyCurrency currency = MoneyCurrency.ofCode("USD");

            String toString = currency.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("USD"));
        }
    }

    @Nested
    @DisplayName("Various ISO-4217 Currencies")
    class VariousCurrenciesTests {

        @ParameterizedTest
        @ValueSource(strings = {"USD", "EUR", "GBP", "JPY", "ARS", "BRL", "MXN", "CLP", "COP", "PEN"})
        @DisplayName("Should accept valid ISO-4217 currency codes")
        void shouldAcceptValidIso4217CurrencyCodes(String code) {
            MoneyCurrency currency = MoneyCurrency.ofCode(code);

            assertNotNull(currency);
            assertEquals(code, currency.code());
        }
    }
}
