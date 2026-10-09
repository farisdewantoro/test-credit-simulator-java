package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;

import java.util.List;

public final class StatusCommand extends AbstractCommand {

    public StatusCommand() {
        super("status", "status", "Show the active sheet's inputs and its last result", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        context.view().status(context.workspace().active());
        return CommandResult.CONTINUE;
    }
}
