package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;

import java.util.List;

public final class ExitCommand extends AbstractCommand {

    public ExitCommand() {
        super("exit", "exit", "Quit the application", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        context.view().info("Bye!");
        return CommandResult.EXIT;
    }
}
