package com.creditsimulator.policy;

import com.creditsimulator.model.VehicleCondition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimumPercentageDownPaymentPolicyTest {

    private final VehiclePolicyFactory factory = new VehiclePolicyFactory();

    @Test
    void newVehicleNeedsThirtyFivePercent() {
        DownPaymentPolicy policy = factory.downPayment(VehicleCondition.BARU);

        assertEquals(0, new BigDecimal("35000000").compareTo(policy.minimumDownPayment(new BigDecimal("100000000"))));
        assertEquals("35%", policy.describe());
    }

    @Test
    void usedVehicleNeedsTwentyFivePercent() {
        DownPaymentPolicy policy = factory.downPayment(VehicleCondition.BEKAS);

        assertEquals(0, new BigDecimal("25000000").compareTo(policy.minimumDownPayment(new BigDecimal("100000000"))));
        assertEquals("25%", policy.describe());
    }

    @Test
    void rejectsFractionOutsideZeroToOne() {
        assertThrows(IllegalArgumentException.class, () -> new MinimumPercentageDownPaymentPolicy(new BigDecimal("1.5")));
        assertThrows(IllegalArgumentException.class, () -> new MinimumPercentageDownPaymentPolicy(new BigDecimal("-0.1")));
    }
}
