package com.creditsimulator.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileInputSourceTest {

    @Test
    void skipsBlankAndCommentLinesAndTracksLineNumbers() {
        FileInputSource input = new FileInputSource("in.txt", List.of("# comment", "", "jenis mobil", "  ", "tenor 3"));

        assertEquals(Optional.empty(), input.lastLocation());
        assertEquals(Optional.of("jenis mobil"), input.next("> "));
        assertEquals(Optional.of("in.txt:3"), input.lastLocation());
        assertEquals(Optional.of("tenor 3"), input.next("> "));
        assertEquals(Optional.of("in.txt:5"), input.lastLocation());
        assertEquals(Optional.empty(), input.next("> "));
        assertFalse(input.interactive());
    }

    @Test
    void readsAFileAsUtf8(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("inputs.txt");
        Files.writeString(file, "show\nexit\n", StandardCharsets.UTF_8);

        FileInputSource input = FileInputSource.open(file);

        assertEquals(Optional.of("show"), input.next(""));
        assertEquals(Optional.of(file + ":1"), input.lastLocation());
    }

    @Test
    void missingFileFailsToOpen(@TempDir Path dir) {
        assertThrows(NoSuchFileException.class, () -> FileInputSource.open(dir.resolve("missing.txt")));
    }
}
