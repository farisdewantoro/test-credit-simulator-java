package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.model.Sheet;

import java.util.List;

public final class SwitchSheetCommand extends AbstractCommand {

    public SwitchSheetCommand() {
        super("switch_sheet", "switch_sheet <name>", "Make <name> the active sheet and show its inputs and result", 1);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        Sheet sheet = context.workspace().switchTo(args.get(0));
        context.view().info("Switched to sheet '" + sheet.name() + "'");
        context.view().status(sheet);
        return CommandResult.CONTINUE;
    }
}
