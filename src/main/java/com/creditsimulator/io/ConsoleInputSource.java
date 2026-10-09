package com.creditsimulator.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

/** Reads lines typed by the user, printing a prompt before each one. */
public final class ConsoleInputSource implements InputSource {

    private final BufferedReader reader;
    private final PrintStream promptOut;

    public ConsoleInputSource(BufferedReader reader, PrintStream promptOut) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.promptOut = Objects.requireNonNull(promptOut, "promptOut");
    }

    @Override
    public Optional<String> next(String prompt) {
        promptOut.print(prompt);
        promptOut.flush();
        try {
            return Optional.ofNullable(reader.readLine());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read from the console", e);
        }
    }

    @Override
    public boolean interactive() {
        return true;
    }
}
