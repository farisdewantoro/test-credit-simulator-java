package com.creditsimulator.support;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.SimulatorController;
import com.creditsimulator.controller.command.CommandFactory;
import com.creditsimulator.exception.RemoteServiceException;
import com.creditsimulator.io.InputSource;
import com.creditsimulator.model.Workspace;
import com.creditsimulator.policy.VehiclePolicyFactory;
import com.creditsimulator.service.ExistingCalculationClient;
import com.creditsimulator.service.InstallmentCalculator;
import com.creditsimulator.service.LoanValidator;
import com.creditsimulator.view.PlainConsoleView;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** A fully wired application around scripted input, with stdout and stderr captured as text. */
public final class TestConsole {

    /** "Now" for every test: 15 June 2026. */
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final Workspace workspace = new Workspace();
    private final CommandContext context;
    private final InputSource input;

    /** Used when a test does not care about {@code load}. */
    private static final ExistingCalculationClient NO_SERVICE = () -> {
        throw new RemoteServiceException("No calculation service in this test");
    };

    public TestConsole(InputSource input) {
        this(input, NO_SERVICE);
    }

    public TestConsole(InputSource input, ExistingCalculationClient client) {
        this.input = input;
        VehiclePolicyFactory policies = new VehiclePolicyFactory();
        this.context = new CommandContext(
                workspace,
                new PlainConsoleView(stream(out), stream(err), false),
                input,
                new LoanValidator(policies, CLOCK),
                new InstallmentCalculator(policies),
                client);
    }

    /** Interactive console that will answer with {@code lines}. */
    public static TestConsole interactive(String... lines) {
        return new TestConsole(new ScriptedInputSource(true, lines));
    }

    /** File-mode console that will read {@code lines}. */
    public static TestConsole file(String... lines) {
        return new TestConsole(new ScriptedInputSource(false, lines));
    }

    /** Runs the controller over all scripted lines and returns the exit code. */
    public int run() {
        return new SimulatorController(input, CommandFactory.withDefaultCommands(), context).run();
    }

    public CommandContext context() {
        return context;
    }

    public Workspace workspace() {
        return workspace;
    }

    public String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    public String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    private static PrintStream stream(ByteArrayOutputStream buffer) {
        return new PrintStream(buffer, true, StandardCharsets.UTF_8);
    }
}
