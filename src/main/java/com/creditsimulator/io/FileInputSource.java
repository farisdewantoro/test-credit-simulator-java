package com.creditsimulator.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Reads commands from a text file, one per line. Blank lines and lines starting with {@code #} are
 * skipped. Remembers line numbers so errors can say {@code file_inputs.txt:7: ...}.
 */
public final class FileInputSource implements InputSource {

    private final String displayName;
    private final List<String> lines;
    private int index;

    FileInputSource(String displayName, List<String> lines) {
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.lines = List.copyOf(lines);
    }

    public static FileInputSource open(Path path) throws IOException {
        return new FileInputSource(path.toString(), Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    @Override
    public Optional<String> next(String prompt) {
        while (index < lines.size()) {
            String line = lines.get(index++);
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean interactive() {
        return false;
    }

    @Override
    public Optional<String> lastLocation() {
        return index == 0 ? Optional.empty() : Optional.of(displayName + ":" + index);
    }
}
