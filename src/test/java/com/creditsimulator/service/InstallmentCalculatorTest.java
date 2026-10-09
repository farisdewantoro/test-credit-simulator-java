package com.creditsimulator.service;

import com.creditsimulator.model.InstallmentSchedule;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;
import com.creditsimulator.model.YearlyInstallment;
import com.creditsimulator.policy.VehiclePolicyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fixtures are the worked examples in TECH_DESIGN.md §4.3. */
class InstallmentCalculatorTest {

    private final InstallmentCalculator calculator = new InstallmentCalculator(new VehiclePolicyFactory());

    @Test
    void reproducesRumusSpreadsheet() {
        InstallmentSchedule schedule = calculator.calculate(application(VehicleType.MOBIL, VehicleCondition.BEKAS,
                "100000000", 3, "25000000"));

        assertYears(schedule,
                new String[]{"0.08", "0.081", "0.086"},
                new String[]{"81000000.00", "58374000.00", "31697082.00"},
                new String[]{"2250000.00", "2432250.00", "2641423.50"});
    }

    @Test
    void calculatesTheWebServicePayload() {
        InstallmentSchedule schedule = calculator.calculate(application(VehicleType.MOBIL, VehicleCondition.BARU,
                "1000000000", 6, "500000000"));

        assertYears(schedule,
                new String[]{"0.08", "0.081", "0.086", "0.087", "0.092", "0.093"},
                new String[]{"540000000.00", "486450000.00", "422627760.00", "344547281.34", "250830420.82",
                        "137078824.98"},
                new String[]{"7500000.00", "8107500.00", "8804745.00", "9570757.82", "10451267.53",
                        "11423235.41"});
    }

    @Test
    void handlesSingleYearMotorcycleLoan() {
        InstallmentSchedule schedule = calculator.calculate(application(VehicleType.MOTOR, VehicleCondition.BEKAS,
                "20000000", 1, "5000000"));

        assertYears(schedule, new String[]{"0.09"}, new String[]{"16350000.00"}, new String[]{"1362500.00"});
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void paysTheLoanOffExactlyByTheEndOfTheTenor(int tenor) {
        LoanApplication application = application(VehicleType.MOTOR, VehicleCondition.BARU, "123456789", tenor,
                "50000000");
        InstallmentSchedule schedule = calculator.calculate(application);

        BigDecimal balance = application.principal();
        for (YearlyInstallment year : schedule.years()) {
            BigDecimal total = balance.multiply(BigDecimal.ONE.add(year.interestRate()));
            balance = total.subtract(year.monthly().multiply(BigDecimal.valueOf(12)));
        }
        assertEquals(tenor, schedule.years().size());
        assertTrue(balance.abs().compareTo(new BigDecimal("0.000001")) < 0, "remaining balance: " + balance);
    }

    @Test
    void keepsTheApplicationOnTheSchedule() {
        LoanApplication application = application(VehicleType.MOBIL, VehicleCondition.BEKAS, "100000000", 3,
                "25000000");

        assertEquals(application, calculator.calculate(application).application());
    }

    private static LoanApplication application(VehicleType type, VehicleCondition condition, String loan, int tenor,
                                               String downPayment) {
        return new LoanApplication(type, condition, 2025, new BigDecimal(loan), tenor, new BigDecimal(downPayment));
    }

    private static void assertYears(InstallmentSchedule schedule, String[] rates, String[] totals, String[] monthly) {
        List<YearlyInstallment> years = schedule.years();
        assertEquals(rates.length, years.size());
        for (int i = 0; i < years.size(); i++) {
            YearlyInstallment year = years.get(i);
            assertEquals(i + 1, year.year());
            assertEquals(0, new BigDecimal(rates[i]).compareTo(year.interestRate()), "rate in year " + (i + 1));
            assertEquals(new BigDecimal(totals[i]), cents(year.totalForYear()), "total in year " + (i + 1));
            assertEquals(new BigDecimal(monthly[i]), cents(year.monthly()), "monthly in year " + (i + 1));
        }
    }

    private static BigDecimal cents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
