package com.creditsimulator.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A complete loan request that has passed every business rule. Only {@code LoanValidator} creates
 * these, so the calculator never sees invalid data.
 */
public record LoanApplication(
        VehicleType vehicleType,
        VehicleCondition condition,
        int vehicleYear,
        BigDecimal loanAmount,
        int tenor,
        BigDecimal downPayment) {

    public LoanApplication {
        Objects.requireNonNull(vehicleType, "vehicleType");
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(loanAmount, "loanAmount");
        Objects.requireNonNull(downPayment, "downPayment");
    }

    /** The amount actually financed: total loan amount minus down payment. */
    public BigDecimal principal() {
        return loanAmount.subtract(downPayment);
    }
}
