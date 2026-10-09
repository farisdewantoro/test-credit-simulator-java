package com.creditsimulator.controller.command;

import com.creditsimulator.controller.command.impl.CalculateCommand;
import com.creditsimulator.controller.command.impl.ExitCommand;
import com.creditsimulator.controller.command.impl.ListSheetsCommand;
import com.creditsimulator.controller.command.impl.LoadCommand;
import com.creditsimulator.controller.command.impl.SaveSheetCommand;
import com.creditsimulator.controller.command.impl.SetConditionCommand;
import com.creditsimulator.controller.command.impl.SetDownPaymentCommand;
import com.creditsimulator.controller.command.impl.SetLoanAmountCommand;
import com.creditsimulator.controller.command.impl.SetTenorCommand;
import com.creditsimulator.controller.command.impl.SetVehicleTypeCommand;
import com.creditsimulator.controller.command.impl.SetYearCommand;
import com.creditsimulator.controller.command.impl.ShowCommand;
import com.creditsimulator.controller.command.impl.StatusCommand;
import com.creditsimulator.controller.command.impl.SwitchSheetCommand;
import com.creditsimulator.exception.UnknownCommandException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Registry of commands and parser that turns a line into the command to run. */
public final class CommandFactory {

    private final Map<String, Command> registry = new LinkedHashMap<>();

    /** Every command the application offers, in the order {@code show} lists them. */
    public static CommandFactory withDefaultCommands() {
        CommandFactory factory = new CommandFactory();
        factory.register(new ShowCommand(factory));
        factory.register(new SetVehicleTypeCommand());
        factory.register(new SetConditionCommand());
        factory.register(new SetYearCommand());
        factory.register(new SetLoanAmountCommand());
        factory.register(new SetTenorCommand());
        factory.register(new SetDownPaymentCommand());
        factory.register(new StatusCommand());
        factory.register(new CalculateCommand());
        factory.register(new LoadCommand());
        factory.register(new SaveSheetCommand());
        factory.register(new SwitchSheetCommand());
        factory.register(new ListSheetsCommand());
        factory.register(new ExitCommand());
        return factory;
    }

    public CommandFactory register(Command command) {
        String key = key(command.descriptor().name());
        if (registry.containsKey(key)) {
            throw new IllegalArgumentException("Command already registered: " + key);
        }
        registry.put(key, command);
        return this;
    }

    /** Splits on whitespace; the first word picks the command (case-insensitive), the rest are arguments. */
    public ParsedCommand parse(String line) {
        String[] words = line.trim().split("\\s+");
        Command command = registry.get(key(words[0]));
        if (command == null) {
            throw new UnknownCommandException(words[0]);
        }
        return new ParsedCommand(command, Arrays.asList(words).subList(1, words.length));
    }

    public List<CommandDescriptor> descriptors() {
        return registry.values().stream().map(Command::descriptor).toList();
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
