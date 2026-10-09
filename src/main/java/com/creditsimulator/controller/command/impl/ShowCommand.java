package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandFactory;
import com.creditsimulator.controller.command.CommandResult;

import java.util.List;
import java.util.Objects;

/** Lists every registered command; generated from the registry so it cannot go stale. */
public final class ShowCommand extends AbstractCommand {

    private final CommandFactory factory;

    public ShowCommand(CommandFactory factory) {
        super("show", "show", "List all available commands", 0);
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        context.view().commands(factory.descriptors());
        return CommandResult.CONTINUE;
    }
}
