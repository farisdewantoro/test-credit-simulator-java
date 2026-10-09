package com.creditsimulator.policy;

import com.creditsimulator.model.VehicleType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SteppedInterestRatePolicyTest {

    private final VehiclePolicyFactory factory = new VehiclePolicyFactory();

    /** The schedule from TECH_DESIGN.md §4.2. */
    @ParameterizedTest(name = "year {0}: Mobil {1}, Motor {2}")
    @CsvSource({
            "1, 0.080, 0.090",
            "2, 0.081, 0.091",
            "3, 0.086, 0.096",
            "4, 0.087, 0.097",
            "5, 0.092, 0.102",
            "6, 0.093, 0.103",
    })
    void followsTheAlternatingStepSchedule(int year, BigDecimal mobil, BigDecimal motor) {
        assertEquals(0, mobil.compareTo(factory.interestRate(VehicleType.MOBIL).rateFor(year)));
        assertEquals(0, motor.compareTo(factory.interestRate(VehicleType.MOTOR).rateFor(year)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 7})
    void rejectsYearsOutsideTheTenor(int year) {
        InterestRatePolicy policy = factory.interestRate(VehicleType.MOBIL);
        assertThrows(IllegalArgumentException.class, () -> policy.rateFor(year));
    }
}
