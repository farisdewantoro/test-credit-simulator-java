package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.VehicleType;
import com.creditsimulator.support.ScriptedInputSource;
import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewCalculationCommandTest {

    @Test
    void asksForEveryFieldInOrderThenCalculates() {
        ScriptedInputSource input = new ScriptedInputSource(true,
                "new", "Mobil", "Bekas", "2022", "100000000", "3", "25000000");
        TestConsole console = new TestConsole(input);

        assertEquals(0, console.run());

        assertEquals(List.of("> ",
                "Jenis Kendaraan (Motor|Mobil): ",
                "Kendaraan (Baru|Bekas): ",
                "Tahun Kendaraan (4 digit): ",
                "Jumlah Pinjaman Total (maks 1000000000): ",
                "Tenor Pinjaman (1-6 thn): ",
                "Jumlah DP: ",
                "> "), input.prompts());
        assertTrue(console.out().contains("tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"));
        assertTrue(console.workspace().active().lastResult().isPresent());
    }

    @Test
    void interactiveInvalidAnswerIsAskedAgain() {
        ScriptedInputSource input = new ScriptedInputSource(true,
                "new", "truk", "Motor", "Bekas", "20a6", "2019", "20000000", "9", "1", "5000000");
        TestConsole console = new TestConsole(input);

        console.run();

        assertTrue(console.err().contains("Error: Vehicle type must be Mobil or Motor. Please try again."));
        assertTrue(console.err().contains("Error: Tenor must be between 1 and 6 years. Please try again."));
        assertTrue(console.out().contains("tahun 1 : Rp. 1,362,500.00/bln , Suku Bunga : 9%"));
    }

    @Test
    void fileModeInvalidAnswerAbortsTheFlowWithoutChangingTheSheet() {
        TestConsole console = TestConsole.file("jenis motor", "new", "Mobil", "truk", "status");

        assertEquals(1, console.run());

        assertTrue(console.err().contains(
                "Error: script:4: Vehicle condition must be Baru or Bekas; 'new' was aborted and nothing was changed"),
                console.err());
        assertEquals(VehicleType.MOTOR, console.workspace().active().draft().vehicleType().orElseThrow());
        assertTrue(console.out().contains("> status"), "the line after the bad answer runs as a command");
    }

    @Test
    void endOfInputDuringTheFlowChangesNothing() {
        TestConsole console = TestConsole.file("new", "Mobil", "Baru");

        assertEquals(1, console.run());

        assertTrue(console.err().contains("Input ended before 'new' was finished; nothing was changed"));
        assertFalse(console.workspace().active().draft().vehicleType().isPresent());
    }

    @Test
    void ruleViolationsAreReportedAfterTheAnswersAreStored() {
        TestConsole console = TestConsole.interactive("new", "Mobil", "Baru", "2026", "100000000", "3", "1000000");

        console.run();

        assertTrue(console.err().contains("DP must be at least 35% of loan amount for a Baru vehicle"));
        assertEquals(3, console.workspace().active().draft().tenor().orElseThrow());
        assertFalse(console.workspace().active().lastResult().isPresent());
    }
}
