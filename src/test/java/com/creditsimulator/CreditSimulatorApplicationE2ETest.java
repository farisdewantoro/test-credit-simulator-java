package com.creditsimulator;

import com.creditsimulator.config.AppConfig;
import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the real application end to end and compares the output with a golden file. */
class CreditSimulatorApplicationE2ETest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    void sampleFileMatchesGoldenOutput() throws IOException {
        int exitCode = run("", "samples/file_inputs.txt");

        String expected = Files.readString(Path.of("src/test/resources/expected_output.txt"), StandardCharsets.UTF_8);
        assertEquals(normalize(expected), normalize(stdout()));
        assertEquals("", stderr());
        assertEquals(0, exitCode);
    }

    @Test
    void fileWithAFailingLineExitsWithOne(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("bad.txt");
        Files.writeString(file, "jenis mobil\n\ntahun 20a6\nshow\n");

        int exitCode = run("", file.toString());

        assertEquals(1, exitCode);
        assertEquals("Error: " + file + ":3: Vehicle year must be a 4-digit number, e.g. 2022",
                stderr().strip());
        assertTrue(stdout().contains("Available commands:"), "later lines still run");
    }

    @Test
    void missingFileExitsWithTwo() {
        int exitCode = run("", "does-not-exist.txt");

        assertEquals(2, exitCode);
        assertEquals("Cannot read file 'does-not-exist.txt': No such file", stderr().strip());
    }

    @Test
    void tooManyArgumentsExitsWithTwo() {
        assertEquals(2, run("", "a.txt", "b.txt"));
        assertTrue(stderr().startsWith("Usage: credit_simulator"));
    }

    @Test
    void helpPrintsUsage() {
        assertEquals(0, run("", "--help"));
        assertTrue(stdout().startsWith("Usage: credit_simulator"));
    }

    @Test
    void interactiveModeReadsStdinUntilEof() {
        int exitCode = run("jenis mobil\nfoo\nstatus\n");

        assertEquals(0, exitCode);
        assertTrue(stdout().startsWith("Credit Simulator."));
        assertTrue(stdout().contains("  jenis   : Mobil"));
        assertTrue(stderr().contains("Unknown command 'foo'"));
    }

    private int run(String stdin, String... args) {
        AppConfig config = AppConfig.withClock(TestConsole.CLOCK);
        return new CreditSimulatorApplication(config,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8))
                .run(args);
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return err.toString(StandardCharsets.UTF_8);
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }
}
