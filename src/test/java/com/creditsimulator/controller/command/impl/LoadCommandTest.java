package com.creditsimulator.controller.command.impl;

import com.creditsimulator.exception.RemoteServiceException;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;
import com.creditsimulator.service.ExistingCalculationClient;
import com.creditsimulator.support.ScriptedInputSource;
import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadCommandTest {

    private static LoanDraft webServicePayload() {
        return new LoanDraft()
                .vehicleType(VehicleType.MOBIL)
                .condition(VehicleCondition.BARU)
                .vehicleYear(2025)
                .loanAmount(new BigDecimal("1000000000"))
                .tenor(6)
                .downPayment(new BigDecimal("500000000"));
    }

    @Test
    void loadsAndAutomaticallyCalculates() {
        TestConsole console = console(stubClient(webServicePayload()), "load");

        assertEquals(0, console.run());

        String out = console.out();
        assertTrue(out.contains("Loaded into sheet 'default': Mobil Baru 2025, loan Rp. 1,000,000,000.00, tenor 6 thn,"
                + " DP Rp. 500,000,000.00"));
        assertTrue(out.contains("tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%"));
        assertTrue(out.contains("tahun 2 : Rp. 8,107,500.00/bln , Suku Bunga : 8,1%"));
        assertTrue(out.contains("tahun 3 : Rp. 8,804,745.00/bln , Suku Bunga : 8,6%"));
        assertTrue(out.contains("tahun 4 : Rp. 9,570,757.82/bln , Suku Bunga : 8,7%"));
        assertTrue(out.contains("tahun 5 : Rp. 10,451,267.53/bln , Suku Bunga : 9,2%"));
        assertTrue(out.contains("tahun 6 : Rp. 11,423,235.41/bln , Suku Bunga : 9,3%"));
        assertTrue(console.workspace().active().lastResult().isPresent());
        assertEquals("", console.err());
    }

    @Test
    void dataBreakingARuleIsLoadedButNotCalculated() {
        LoanDraft tooOld = webServicePayload().vehicleYear(2020);
        TestConsole console = console(stubClient(tooOld), "load");

        console.run();

        assertTrue(console.err().contains("A new (Baru) vehicle cannot be older than 2025, got 2020"));
        assertEquals(2020, console.workspace().active().draft().vehicleYear().orElseThrow());
        assertFalse(console.workspace().active().lastResult().isPresent());
    }

    @Test
    void serviceFailureLeavesTheSheetUnchanged() {
        ExistingCalculationClient failing = () -> {
            throw new RemoteServiceException("Calculation service returned HTTP 503");
        };
        TestConsole console = console(failing, "jenis motor", "load");

        console.run();

        assertTrue(console.err().contains("Error: Calculation service returned HTTP 503"));
        assertEquals(VehicleType.MOTOR, console.workspace().active().draft().vehicleType().orElseThrow());
    }

    private static ExistingCalculationClient stubClient(LoanDraft draft) {
        return draft::copy;
    }

    private static TestConsole console(ExistingCalculationClient client, String... lines) {
        return new TestConsole(new ScriptedInputSource(true, lines), client);
    }
}
