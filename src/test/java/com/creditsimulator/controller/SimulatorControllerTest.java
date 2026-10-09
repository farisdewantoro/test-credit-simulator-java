package com.creditsimulator.controller;

import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorControllerTest {

    private static final String[] USED_CAR = {
            "jenis mobil", "kondisi bekas", "tahun 2022", "nominal 100000000", "tenor 3", "dp 25000000"};

    @Test
    void setsFieldsAndCalculates() {
        TestConsole console = TestConsole.interactive(concat(USED_CAR, "calculate"));

        assertEquals(0, console.run());
        assertTrue(console.out().contains("tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%"));
        assertTrue(console.out().contains("tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%"));
        assertTrue(console.out().contains("tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"));
        assertEquals("", console.err());
    }

    @Test
    void fieldsCanBeEnteredInAnyOrder() {
        TestConsole console = TestConsole.interactive(
                "dp 25000000", "tenor 3", "nominal 100000000", "tahun 2022", "kondisi bekas", "jenis mobil",
                "calculate");

        console.run();

        assertTrue(console.out().contains("tahun 3 : Rp. 2,641,423.50/bln"));
    }

    @Test
    void errorsDoNotEndTheSession() {
        TestConsole console = TestConsole.interactive(concat(new String[]{"foo", "tahun 20a6"}, concat(USED_CAR,
                "calculate")));

        assertEquals(0, console.run());
        assertTrue(console.err().contains("Error: Unknown command 'foo'. Type 'show' to list commands."));
        assertTrue(console.err().contains("Error: Vehicle year must be a 4-digit number, e.g. 2022"));
        assertTrue(console.out().contains("tahun 1 : Rp. 2,250,000.00/bln"));
    }

    @Test
    void calculateWithMissingFieldsListsThem() {
        TestConsole console = TestConsole.interactive("jenis mobil", "calculate");

        console.run();

        assertTrue(console.err().contains("Error: Missing: kondisi, tahun, nominal, tenor, dp"));
    }

    @Test
    void wrongArgumentCountShowsUsage() {
        TestConsole console = TestConsole.interactive("jenis", "calculate now");

        console.run();

        assertTrue(console.err().contains("Error: Usage: jenis <Mobil|Motor>"));
        assertTrue(console.err().contains("Error: Usage: calculate"));
    }

    @Test
    void exitStopsReadingFurtherLines() {
        TestConsole console = TestConsole.interactive("exit", "foo");

        assertEquals(0, console.run());
        assertEquals("", console.err());
    }

    @Test
    void blankLinesAndCommentsAreIgnored() {
        TestConsole console = TestConsole.file("", "   ", "# a comment", "status");

        assertEquals(0, console.run());
        assertEquals("", console.err());
        assertFalse(console.out().contains("# a comment"));
    }

    @Test
    void fileModeEchoesCommandsAndPrefixesErrorsWithTheLine() {
        TestConsole console = TestConsole.file("jenis mobil", "tenor 9");

        assertEquals(1, console.run());
        assertTrue(console.out().contains("> jenis mobil"));
        assertTrue(console.err().contains("Error: script:2: Tenor must be between 1 and 6 years"));
    }

    @Test
    void editingAnInputClearsTheLastResult() {
        TestConsole console = TestConsole.interactive(concat(USED_CAR, "calculate", "tenor 2", "status"));

        console.run();

        assertTrue(console.out().contains("result  : not calculated"));
        assertFalse(console.workspace().active().lastResult().isPresent());
    }

    @Test
    void statusShowsUnsetFieldsAsDash() {
        TestConsole console = TestConsole.interactive("jenis motor", "status");

        console.run();

        assertTrue(console.out().contains("  jenis   : Motor"));
        assertTrue(console.out().contains("  kondisi : -"));
        assertTrue(console.out().contains("  dp      : -"));
    }

    private static String[] concat(String[] first, String... rest) {
        String[] all = new String[first.length + rest.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(rest, 0, all, first.length, rest.length);
        return all;
    }
}
