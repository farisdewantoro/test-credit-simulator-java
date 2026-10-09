package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Field-level parsing shared by the console commands and the web-service mapping. It only checks the
 * format of a single value; rules that span several fields live in {@code LoanValidator}.
 */
public final class InputParser {

    public static final int MIN_TENOR = 1;
    public static final int MAX_TENOR = 6;

    /** 15 digits is far above any valid amount and keeps the value well inside {@code long}. */
    private static final int MAX_AMOUNT_DIGITS = 15;

    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern YEAR = Pattern.compile("\\d{4}");

    private InputParser() {
    }

    /** A vehicle year: exactly four digits. */
    public static int parseYear(String text) {
        String value = trim(text);
        if (!YEAR.matcher(value).matches()) {
            throw new InvalidInputException("Vehicle year must be a 4-digit number, e.g. 2022");
        }
        return Integer.parseInt(value);
    }

    /** A tenor in whole years between {@value #MIN_TENOR} and {@value #MAX_TENOR}. */
    public static int parseTenor(String text) {
        String value = trim(text);
        if (!DIGITS.matcher(value).matches() || value.length() > 2) {
            throw new InvalidInputException("Tenor must be a whole number of years between 1 and 6");
        }
        int tenor = Integer.parseInt(value);
        if (tenor < MIN_TENOR || tenor > MAX_TENOR) {
            throw new InvalidInputException("Tenor must be between 1 and 6 years");
        }
        return tenor;
    }

    /**
     * A money amount in rupiah written as plain digits, e.g. {@code 350000000}. Separators such as
     * {@code .} and {@code ,} are rejected because they mean different things in Indonesian and US formats.
     */
    public static BigDecimal parseAmount(String text, String fieldName) {
        String value = trim(text);
        if (!DIGITS.matcher(value).matches()) {
            throw new InvalidInputException(fieldName + " must be a whole number of rupiah written with digits only, e.g. 100000000");
        }
        String significant = value.replaceFirst("^0+(?=\\d)", "");
        if (significant.length() > MAX_AMOUNT_DIGITS) {
            throw new InvalidInputException(fieldName + " is too large");
        }
        BigDecimal amount = new BigDecimal(significant);
        if (amount.signum() == 0) {
            throw new InvalidInputException(fieldName + " must be greater than 0");
        }
        return amount;
    }

    private static String trim(String text) {
        return text == null ? "" : text.trim();
    }
}
