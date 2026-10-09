package com.creditsimulator.service;

import com.creditsimulator.model.InstallmentSchedule;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.YearlyInstallment;
import com.creditsimulator.policy.InterestRatePolicy;
import com.creditsimulator.policy.VehiclePolicyFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the yearly schedule as in {@code Rumus.xlsx}: each year, interest is charged on the balance
 * left from the previous year and the total is spread over all remaining months. Pure: no I/O, no clock.
 */
public final class InstallmentCalculator {

    private static final MathContext PRECISION = MathContext.DECIMAL128;
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private final VehiclePolicyFactory policies;

    public InstallmentCalculator(VehiclePolicyFactory policies) {
        this.policies = Objects.requireNonNull(policies, "policies");
    }

    public InstallmentSchedule calculate(LoanApplication application) {
        InterestRatePolicy ratePolicy = policies.interestRate(application.vehicleType());
        int tenor = application.tenor();
        List<YearlyInstallment> years = new ArrayList<>(tenor);

        BigDecimal balance = application.principal();
        for (int year = 1; year <= tenor; year++) {
            BigDecimal rate = ratePolicy.rateFor(year);
            BigDecimal total = balance.multiply(BigDecimal.ONE.add(rate), PRECISION);
            BigDecimal remainingMonths = BigDecimal.valueOf(12L * (tenor - year + 1));
            BigDecimal monthly = total.divide(remainingMonths, PRECISION);
            balance = total.subtract(monthly.multiply(MONTHS_PER_YEAR, PRECISION), PRECISION);
            years.add(new YearlyInstallment(year, rate, total, monthly));
        }
        return new InstallmentSchedule(application, years);
    }
}
