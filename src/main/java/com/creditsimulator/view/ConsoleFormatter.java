package com.creditsimulator.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Number formats from the spec sample: money as {@code Rp. 3,500,000.00} (comma thousands, dot
 * decimals, rounded half-up) and rates as {@code 8,1%} (comma decimals, trailing zeros dropped).
 */
public final class ConsoleFormatter {

    private ConsoleFormatter() {
    }

    public static String money(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return "Rp. " + format.format(amount);
    }

    /** @param rate a fraction, e.g. {@code 0.081} */
    public static String rate(BigDecimal rate) {
        return rate.movePointRight(2).stripTrailingZeros().toPlainString().replace('.', ',') + "%";
    }
}
