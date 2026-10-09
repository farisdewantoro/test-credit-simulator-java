package com.creditsimulator.io;

import java.util.Optional;

/** Where command lines come from: the interactive console or an input file. */
public interface InputSource {

    /**
     * Reads the next line, showing {@code prompt} first when interactive.
     *
     * @return the line, or empty at end of input
     */
    Optional<String> next(String prompt);

    /** True when a person is typing, so invalid answers can be re-asked instead of aborting. */
    boolean interactive();

    /** Where the last line came from, e.g. {@code file_inputs.txt:7}; empty for the console. */
    default Optional<String> lastLocation() {
        return Optional.empty();
    }
}
