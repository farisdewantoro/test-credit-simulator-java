package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;

import java.util.List;

public final class ListSheetsCommand extends AbstractCommand {

    public ListSheetsCommand() {
        super("list_sheets", "list_sheets", "List all sheets; '*' marks the active one", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        context.view().sheets(context.workspace().list(), context.workspace().active());
        return CommandResult.CONTINUE;
    }
}
