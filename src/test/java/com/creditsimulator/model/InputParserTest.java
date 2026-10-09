package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputParserTest {

    @Test
    void parsesPlainDigitAmounts() {
        assertEquals(new BigDecimal("350000000"), InputParser.parseAmount("350000000", "Loan amount"));
        assertEquals(new BigDecimal("1"), InputParser.parseAmount(" 1 ", "Loan amount"));
        assertEquals(new BigDecimal("1000"), InputParser.parseAmount("0001000", "Loan amount"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1.000", "1,000", "-5", "1e9", "12a", "Rp100", "+5", "10.5"})
    void rejectsAmountsThatAreNotPlainDigits(String input) {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> InputParser.parseAmount(input, "Loan amount"));
        assertEquals("Loan amount must be a whole number of rupiah written with digits only, e.g. 100000000",
                error.getMessage());
    }

    @Test
    void rejectsOverflowingAmounts() {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> InputParser.parseAmount("99999999999999999999999", "DP"));
        assertEquals("DP is too large", error.getMessage());
    }

    @Test
    void rejectsZeroAmount() {
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> InputParser.parseAmount("000", "DP"));
        assertEquals("DP must be greater than 0", error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2022", " 1999 ", "0001", "9999"})
    void acceptsFourDigitYears(String input) {
        assertEquals(Integer.parseInt(input.trim()), InputParser.parseYear(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"22", "20226", "20a6", "-202", "2022.0"})
    void rejectsYearsThatAreNotFourDigits(String input) {
        assertThrows(InvalidInputException.class, () -> InputParser.parseYear(input));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void acceptsTenorOneToSix(int tenor) {
        assertEquals(tenor, InputParser.parseTenor(String.valueOf(tenor)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "7", "12"})
    void rejectsTenorOutOfRange(String input) {
        InvalidInputException error = assertThrows(InvalidInputException.class, () -> InputParser.parseTenor(input));
        assertEquals("Tenor must be between 1 and 6 years", error.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "-1", "1.5", "999999999999"})
    void rejectsTenorThatIsNotAWholeNumber(String input) {
        assertThrows(InvalidInputException.class, () -> InputParser.parseTenor(input));
    }
}
