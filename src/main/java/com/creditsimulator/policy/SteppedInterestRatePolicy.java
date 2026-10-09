package com.creditsimulator.policy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Starts from a base rate and adds a step every year after the first. The step alternates: one amount
 * going into an even year (2, 4, 6) and another going into an odd year (3, 5). With steps of 0.1% and
 * 0.5% this reproduces the spec sample and {@code Rumus.xlsx}: 8% → 8.1% → 8.6% → 8.7% → …
 */
public final class SteppedInterestRatePolicy implements InterestRatePolicy {

    private final BigDecimal baseRate;
    private final BigDecimal stepIntoEvenYear;
    private final BigDecimal stepIntoOddYear;
    private final int maxYear;

    public SteppedInterestRatePolicy(BigDecimal baseRate, BigDecimal stepIntoEvenYear,
                                     BigDecimal stepIntoOddYear, int maxYear) {
        this.baseRate = Objects.requireNonNull(baseRate, "baseRate");
        this.stepIntoEvenYear = Objects.requireNonNull(stepIntoEvenYear, "stepIntoEvenYear");
        this.stepIntoOddYear = Objects.requireNonNull(stepIntoOddYear, "stepIntoOddYear");
        if (maxYear < 1) {
            throw new IllegalArgumentException("maxYear must be at least 1");
        }
        this.maxYear = maxYear;
    }

    @Override
    public BigDecimal rateFor(int year) {
        if (year < 1 || year > maxYear) {
            throw new IllegalArgumentException("year must be between 1 and " + maxYear + ": " + year);
        }
        BigDecimal rate = baseRate;
        for (int y = 2; y <= year; y++) {
            rate = rate.add(y % 2 == 0 ? stepIntoEvenYear : stepIntoOddYear);
        }
        return rate;
    }
}
