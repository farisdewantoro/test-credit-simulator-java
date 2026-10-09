package com.creditsimulator.controller.command.impl;

import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SheetCommandsTest {

    @Test
    void saveSwitchAndListSheets() {
        TestConsole console = TestConsole.interactive(
                "jenis mobil", "kondisi bekas", "tahun 2022", "nominal 100000000", "tenor 3", "dp 25000000",
                "calculate", "save_sheet mobil-bekas",
                "switch_sheet default", "jenis motor", "tahun 2019", "nominal 20000000", "tenor 1", "dp 5000000",
                "save_sheet motor-bekas",
                "list_sheets", "switch_sheet MOBIL-BEKAS");

        assertEquals(0, console.run());

        String out = console.out();
        assertTrue(out.contains("Saved sheet 'mobil-bekas' from 'default'; 'mobil-bekas' is now the active sheet"));
        assertTrue(out.contains("  default      Motor/Bekas/2019  Rp. 20,000,000.00   1 thn  not calculated"), out);
        assertTrue(out.contains("  mobil-bekas  Mobil/Bekas/2022  Rp. 100,000,000.00  3 thn  ✔ calculated"), out);
        assertTrue(out.contains("* motor-bekas  Motor/Bekas/2019  Rp. 20,000,000.00   1 thn  not calculated"), out);
        assertTrue(out.endsWith(String.join(System.lineSeparator(),
                "Switched to sheet 'mobil-bekas'",
                "Sheet 'mobil-bekas'",
                "  jenis   : Mobil",
                "  kondisi : Bekas",
                "  tahun   : 2022",
                "  nominal : Rp. 100,000,000.00",
                "  tenor   : 3 thn",
                "  dp      : Rp. 25,000,000.00",
                "  result  :",
                "    tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%",
                "    tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%",
                "    tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%",
                "")), out);
        assertEquals("", console.err());
    }

    @Test
    void overwritingASheetSaysSo() {
        TestConsole console = TestConsole.interactive("save_sheet a", "switch_sheet default", "save_sheet A");

        console.run();

        assertTrue(console.out().contains("Overwrote sheet 'A' from 'default'; 'A' is now the active sheet"));
    }

    @Test
    void switchingToAnUnknownSheetIsAnError() {
        TestConsole console = TestConsole.interactive("switch_sheet nope");

        console.run();

        assertTrue(console.err().contains("Error: Sheet 'nope' not found. Available sheets: default"));
    }

    @Test
    void sheetCommandsRequireAName() {
        TestConsole console = TestConsole.interactive("save_sheet", "switch_sheet");

        console.run();

        assertTrue(console.err().contains("Error: Usage: save_sheet <name>"));
        assertTrue(console.err().contains("Error: Usage: switch_sheet <name>"));
    }
}
