package com.creditsimulator.view;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleFormatterTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "3500000          | Rp. 3,500,000.00",
            "2641423.5        | Rp. 2,641,423.50",
            "9570757.8159722  | Rp. 9,570,757.82",
            "0.005            | Rp. 0.01",
            "0                | Rp. 0.00",
            "1000000000       | Rp. 1,000,000,000.00",
    })
    void formatsMoneyLikeTheSpecSample(BigDecimal amount, String expected) {
        assertEquals(expected, ConsoleFormatter.money(amount));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "0.08   | 8%",
            "0.080  | 8%",
            "0.081  | 8,1%",
            "0.086  | 8,6%",
            "0.102  | 10,2%",
            "0.1    | 10%",
    })
    void formatsRatesWithCommaDecimals(BigDecimal rate, String expected) {
        assertEquals(expected, ConsoleFormatter.rate(rate));
    }
}
