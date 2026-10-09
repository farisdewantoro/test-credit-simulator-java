package com.creditsimulator.model;

import java.math.BigDecimal;

/**
 * One year of the schedule, kept at full precision; rounding happens only when displayed.
 *
 * @param year         1-based year of the tenor
 * @param interestRate the rate for that year as a fraction, e.g. {@code 0.081}
 * @param totalForYear balance carried into the year plus that year's interest
 * @param monthly      installment to pay each month of that year
 */
public record YearlyInstallment(int year, BigDecimal interestRate, BigDecimal totalForYear, BigDecimal monthly) {
}
