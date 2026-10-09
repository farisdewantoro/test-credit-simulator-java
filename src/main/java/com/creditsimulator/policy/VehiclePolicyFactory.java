package com.creditsimulator.policy;

import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/** The single place that decides which interest-rate and down-payment rules apply to a vehicle. */
public final class VehiclePolicyFactory {

    private static final BigDecimal STEP_INTO_EVEN_YEAR = new BigDecimal("0.001");
    private static final BigDecimal STEP_INTO_ODD_YEAR = new BigDecimal("0.005");

    private final Map<VehicleType, InterestRatePolicy> interestRates = new EnumMap<>(VehicleType.class);
    private final Map<VehicleCondition, DownPaymentPolicy> downPayments = new EnumMap<>(VehicleCondition.class);

    public VehiclePolicyFactory() {
        interestRates.put(VehicleType.MOBIL, steppedFrom("0.08"));
        interestRates.put(VehicleType.MOTOR, steppedFrom("0.09"));
        downPayments.put(VehicleCondition.BARU, new MinimumPercentageDownPaymentPolicy(new BigDecimal("0.35")));
        downPayments.put(VehicleCondition.BEKAS, new MinimumPercentageDownPaymentPolicy(new BigDecimal("0.25")));
    }

    public InterestRatePolicy interestRate(VehicleType vehicleType) {
        return interestRates.get(vehicleType);
    }

    public DownPaymentPolicy downPayment(VehicleCondition condition) {
        return downPayments.get(condition);
    }

    private static InterestRatePolicy steppedFrom(String baseRate) {
        return new SteppedInterestRatePolicy(new BigDecimal(baseRate), STEP_INTO_EVEN_YEAR, STEP_INTO_ODD_YEAR,
                InputParser.MAX_TENOR);
    }
}
