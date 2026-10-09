package com.creditsimulator.service;

import com.creditsimulator.exception.ValidationException;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;
import com.creditsimulator.policy.VehiclePolicyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanValidatorTest {

    /** Fixed "now" in 2026, so currentYear - 1 = 2025 and currentYear + 1 = 2027. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    private final LoanValidator validator = new LoanValidator(new VehiclePolicyFactory(), CLOCK);

    @Test
    void buildsAnApplicationFromAValidDraft() {
        LoanApplication application = validator.validate(usedCar());

        assertEquals(new LoanApplication(VehicleType.MOBIL, VehicleCondition.BEKAS, 2022,
                new BigDecimal("100000000"), 3, new BigDecimal("25000000")), application);
    }

    @Test
    void reportsMissingFields() {
        LoanDraft draft = usedCar().tenor(null).downPayment(null);

        assertEquals(List.of("Missing: tenor, dp"), violations(draft));
    }

    @Test
    void newVehicleFromLastYearIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(newCar().vehicleYear(2025)));
    }

    @Test
    void newVehicleOlderThanLastYearIsRejected() {
        assertEquals(List.of("A new (Baru) vehicle cannot be older than 2025, got 2024"),
                violations(newCar().vehicleYear(2024)));
    }

    @Test
    void usedVehicleMayBeOld() {
        assertDoesNotThrow(() -> validator.validate(usedCar().vehicleYear(1990)));
    }

    @Test
    void yearAfterNextYearIsRejected() {
        assertDoesNotThrow(() -> validator.validate(newCar().vehicleYear(2027)));
        assertEquals(List.of("Vehicle year cannot be later than 2027"), violations(newCar().vehicleYear(2028)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 6})
    void tenorAtTheBoundsIsAccepted(int tenor) {
        assertDoesNotThrow(() -> validator.validate(usedCar().tenor(tenor)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7})
    void tenorOutsideTheBoundsIsRejected(int tenor) {
        assertEquals(List.of("Tenor must be between 1 and 6 years"), violations(usedCar().tenor(tenor)));
    }

    @Test
    void loanOfExactlyOneBillionIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(usedCar()
                .loanAmount(new BigDecimal("1000000000"))
                .downPayment(new BigDecimal("250000000"))));
    }

    @Test
    void loanAboveOneBillionIsRejected() {
        assertEquals(List.of("Loan amount cannot exceed Rp. 1,000,000,000.00"), violations(usedCar()
                .loanAmount(new BigDecimal("1000000001"))
                .downPayment(new BigDecimal("500000000"))));
    }

    @Test
    void newVehicleDownPaymentOfExactlyThirtyFivePercentIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(newCar().downPayment(new BigDecimal("35000000"))));
    }

    @Test
    void newVehicleDownPaymentOneRupiahShortIsRejected() {
        assertEquals(List.of("DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 35,000,000.00)"),
                violations(newCar().downPayment(new BigDecimal("34999999"))));
    }

    @Test
    void usedVehicleDownPaymentOfExactlyTwentyFivePercentIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(usedCar().downPayment(new BigDecimal("25000000"))));
    }

    @Test
    void usedVehicleDownPaymentBelowTwentyFivePercentIsRejected() {
        assertEquals(List.of("DP must be at least 25% of loan amount for a Bekas vehicle (min Rp. 25,000,000.00)"),
                violations(usedCar().downPayment(new BigDecimal("24999999"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100000000", "150000000"})
    void downPaymentCoveringTheWholeLoanIsRejected(String downPayment) {
        assertEquals(List.of("DP must be less than the loan amount (Rp. 100,000,000.00)"),
                violations(usedCar().downPayment(new BigDecimal(downPayment))));
    }

    @Test
    void reportsEveryViolationTogether() {
        LoanDraft draft = newCar()
                .vehicleYear(2020)
                .tenor(9)
                .downPayment(new BigDecimal("1000000"));

        ValidationException error = assertThrows(ValidationException.class, () -> validator.validate(draft));

        assertEquals(List.of(
                "A new (Baru) vehicle cannot be older than 2025, got 2020",
                "Tenor must be between 1 and 6 years",
                "DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 35,000,000.00)"),
                error.violations());
        assertEquals(String.join(System.lineSeparator(),
                "Cannot calculate, 3 problems found:",
                "  - A new (Baru) vehicle cannot be older than 2025, got 2020",
                "  - Tenor must be between 1 and 6 years",
                "  - DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 35,000,000.00)"),
                error.getMessage());
    }

    @Test
    void checksPresentFieldsEvenWhenOthersAreMissing() {
        LoanDraft draft = new LoanDraft().tenor(8);

        assertEquals(List.of("Missing: jenis, kondisi, tahun, nominal, dp", "Tenor must be between 1 and 6 years"),
                violations(draft));
    }

    private List<String> violations(LoanDraft draft) {
        return assertThrows(ValidationException.class, () -> validator.validate(draft)).violations();
    }

    private static LoanDraft usedCar() {
        return new LoanDraft()
                .vehicleType(VehicleType.MOBIL)
                .condition(VehicleCondition.BEKAS)
                .vehicleYear(2022)
                .loanAmount(new BigDecimal("100000000"))
                .tenor(3)
                .downPayment(new BigDecimal("25000000"));
    }

    private static LoanDraft newCar() {
        return usedCar()
                .condition(VehicleCondition.BARU)
                .vehicleYear(2026)
                .downPayment(new BigDecimal("35000000"));
    }
}
