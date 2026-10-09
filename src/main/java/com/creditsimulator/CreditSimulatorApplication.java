package com.creditsimulator;

import com.creditsimulator.config.AppConfig;
import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.SimulatorController;
import com.creditsimulator.controller.command.CommandFactory;
import com.creditsimulator.io.ConsoleInputSource;
import com.creditsimulator.io.FileInputSource;
import com.creditsimulator.io.InputSource;
import com.creditsimulator.model.Workspace;
import com.creditsimulator.policy.VehiclePolicyFactory;
import com.creditsimulator.service.InstallmentCalculator;
import com.creditsimulator.service.LoanValidator;
import com.creditsimulator.view.ConsoleView;
import com.creditsimulator.view.PlainConsoleView;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/** Entry point: parses the arguments, wires the application together and returns the exit code. */
public final class CreditSimulatorApplication {

    /** Exit code for bad arguments or an unreadable input file. */
    public static final int EXIT_USAGE = 2;

    private static final String USAGE = String.join(System.lineSeparator(),
            "Usage: credit_simulator [file_inputs.txt]",
            "  (no argument)    interactive mode; type 'show' to list commands",
            "  file_inputs.txt  run the commands in the file, one per line ('#' starts a comment)");

    private final AppConfig config;
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    public CreditSimulatorApplication(AppConfig config, InputStream in, PrintStream out, PrintStream err) {
        this.config = config;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        int exitCode = new CreditSimulatorApplication(AppConfig.fromEnvironment(System.getenv()), System.in, out, err)
                .run(args);
        System.exit(exitCode);
    }

    public int run(String[] args) {
        if (args.length == 0) {
            InputSource input = new ConsoleInputSource(
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), out);
            out.println("Credit Simulator. Type 'show' to list commands or 'exit' to quit.");
            return controller(input).run();
        }
        if (args.length == 1 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            out.println(USAGE);
            return SimulatorController.EXIT_OK;
        }
        if (args.length > 1) {
            err.println(USAGE);
            return EXIT_USAGE;
        }
        FileInputSource input;
        try {
            input = FileInputSource.open(Path.of(args[0]));
        } catch (IOException | RuntimeException e) {
            err.println("Cannot read file '" + args[0] + "': " + reason(e));
            return EXIT_USAGE;
        }
        return controller(input).run();
    }

    private SimulatorController controller(InputSource input) {
        VehiclePolicyFactory policies = new VehiclePolicyFactory();
        ConsoleView view = new PlainConsoleView(out, err, config.debug());
        CommandContext context = new CommandContext(
                new Workspace(),
                view,
                input,
                new LoanValidator(policies, config.clock()),
                new InstallmentCalculator(policies));
        return new SimulatorController(input, CommandFactory.withDefaultCommands(), context);
    }

    private static String reason(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "No such file";
        }
        if (e instanceof AccessDeniedException) {
            return "Permission denied";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
