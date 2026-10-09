package com.creditsimulator.policy;

import java.math.BigDecimal;

/** The annual interest rate charged in a given year of the loan. */
public interface InterestRatePolicy {

    /**
     * @param year 1-based year of the tenor
     * @return the rate as a fraction, e.g. {@code 0.081} for 8.1%
     */
    BigDecimal rateFor(int year);
}
