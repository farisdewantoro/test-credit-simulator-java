package com.creditsimulator.controller;

import com.creditsimulator.controller.command.CommandFactory;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.controller.command.ParsedCommand;
import com.creditsimulator.exception.CreditSimulatorException;
import com.creditsimulator.io.InputSource;

import java.util.Objects;
import java.util.Optional;

/**
 * The read → parse → execute loop. An error in one command is reported and the session goes on; it
 * never ends the program.
 */
public final class SimulatorController {

    public static final String PROMPT = "> ";

    /** Exit code when every line succeeded, or when running interactively. */
    public static final int EXIT_OK = 0;
    /** Exit code when reading from a file and at least one line failed. */
    public static final int EXIT_LINE_FAILED = 1;

    private final InputSource input;
    private final CommandFactory factory;
    private final CommandContext context;

    public SimulatorController(InputSource input, CommandFactory factory, CommandContext context) {
        this.input = Objects.requireNonNull(input, "input");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.context = Objects.requireNonNull(context, "context");
    }

    public int run() {
        int failures = 0;
        while (true) {
            Optional<String> next = input.next(PROMPT);
            if (next.isEmpty()) {
                break;
            }
            String line = next.get().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (!input.interactive()) {
                context.view().echo(PROMPT + line);
            }
            try {
                ParsedCommand parsed = factory.parse(line);
                if (parsed.command().execute(context, parsed.args()) == CommandResult.EXIT) {
                    break;
                }
            } catch (CreditSimulatorException e) {
                failures++;
                context.view().error(locationPrefix() + e.getMessage());
            } catch (RuntimeException e) {
                failures++;
                context.view().unexpectedError(locationPrefix(), e);
            }
        }
        return !input.interactive() && failures > 0 ? EXIT_LINE_FAILED : EXIT_OK;
    }

    private String locationPrefix() {
        return input.lastLocation().map(location -> location + ": ").orElse("");
    }
}
