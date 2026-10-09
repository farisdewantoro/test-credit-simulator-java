package com.creditsimulator.support;

import com.creditsimulator.io.InputSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/** Feeds a fixed list of lines, and records the prompts it was asked with. */
public final class ScriptedInputSource implements InputSource {

    private final Iterator<String> lines;
    private final boolean interactive;
    private final List<String> prompts = new ArrayList<>();
    private int lineNumber;

    public ScriptedInputSource(boolean interactive, String... lines) {
        this.lines = List.of(lines).iterator();
        this.interactive = interactive;
    }

    @Override
    public Optional<String> next(String prompt) {
        prompts.add(prompt);
        if (!lines.hasNext()) {
            return Optional.empty();
        }
        lineNumber++;
        return Optional.of(lines.next());
    }

    @Override
    public boolean interactive() {
        return interactive;
    }

    @Override
    public Optional<String> lastLocation() {
        return interactive ? Optional.empty() : Optional.of("script:" + lineNumber);
    }

    public List<String> prompts() {
        return prompts;
    }
}
