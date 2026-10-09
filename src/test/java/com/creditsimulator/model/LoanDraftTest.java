package com.creditsimulator.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanDraftTest {

    @Test
    void reportsMissingFieldsByCommandNameInInputOrder() {
        LoanDraft draft = new LoanDraft().vehicleType(VehicleType.MOBIL).vehicleYear(2022);

        assertEquals(List.of("kondisi", "nominal", "tenor", "dp"), draft.missingFields());
    }

    @Test
    void completeDraftHasNoMissingFields() {
        LoanDraft draft = new LoanDraft()
                .vehicleType(VehicleType.MOTOR)
                .condition(VehicleCondition.BEKAS)
                .vehicleYear(2019)
                .loanAmount(new BigDecimal("20000000"))
                .tenor(1)
                .downPayment(new BigDecimal("5000000"));

        assertTrue(draft.missingFields().isEmpty());
    }

    @Test
    void copyIsIndependent() {
        LoanDraft original = new LoanDraft().vehicleType(VehicleType.MOBIL).tenor(3);
        LoanDraft copy = original.copy();

        copy.vehicleType(VehicleType.MOTOR).tenor(1);

        assertEquals(VehicleType.MOBIL, original.vehicleType().orElseThrow());
        assertEquals(3, original.tenor().orElseThrow());
    }

    @Test
    void principalIsLoanAmountMinusDownPayment() {
        LoanApplication application = new LoanApplication(VehicleType.MOBIL, VehicleCondition.BEKAS, 2022,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000"));

        assertEquals(new BigDecimal("75000000"), application.principal());
    }
}
