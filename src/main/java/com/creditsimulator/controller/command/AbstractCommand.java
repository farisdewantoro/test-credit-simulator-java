package com.creditsimulator.controller.command;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.exception.InvalidInputException;

import java.util.List;

/** Holds the descriptor and checks the number of arguments before running the command. */
public abstract class AbstractCommand implements Command {

    private final CommandDescriptor descriptor;
    private final int argumentCount;

    protected AbstractCommand(String name, String usage, String description, int argumentCount) {
        this.descriptor = new CommandDescriptor(name, usage, description);
        this.argumentCount = argumentCount;
    }

    @Override
    public final CommandDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public final CommandResult execute(CommandContext context, List<String> args) {
        if (args.size() != argumentCount) {
            throw new InvalidInputException("Usage: " + descriptor.usage());
        }
        return run(context, args);
    }

    protected abstract CommandResult run(CommandContext context, List<String> args);
}
