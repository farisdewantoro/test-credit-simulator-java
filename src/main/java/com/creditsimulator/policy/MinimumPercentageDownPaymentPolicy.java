package com.creditsimulator.policy;

import java.math.BigDecimal;
import java.util.Objects;

/** The down payment must be at least a fixed percentage of the total loan amount. */
public final class MinimumPercentageDownPaymentPolicy implements DownPaymentPolicy {

    private final BigDecimal minimumFraction;

    /** @param minimumFraction e.g. {@code 0.35} for 35% */
    public MinimumPercentageDownPaymentPolicy(BigDecimal minimumFraction) {
        Objects.requireNonNull(minimumFraction, "minimumFraction");
        if (minimumFraction.signum() < 0 || minimumFraction.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("minimumFraction must be between 0 and 1: " + minimumFraction);
        }
        this.minimumFraction = minimumFraction;
    }

    @Override
    public BigDecimal minimumDownPayment(BigDecimal loanAmount) {
        return loanAmount.multiply(minimumFraction);
    }

    @Override
    public String describe() {
        return minimumFraction.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }
}
