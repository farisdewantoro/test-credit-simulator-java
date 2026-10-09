package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.model.Sheet;
import com.creditsimulator.model.Workspace;

import java.util.List;

public final class SaveSheetCommand extends AbstractCommand {

    public SaveSheetCommand() {
        super("save_sheet", "save_sheet <name>",
                "Save the active sheet's inputs and result as <name> and make it the active sheet", 1);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        Workspace workspace = context.workspace();
        String from = workspace.active().name();
        boolean overwriting = workspace.contains(args.get(0));
        Sheet saved = workspace.saveAs(args.get(0));
        context.view().info((overwriting ? "Overwrote sheet '" : "Saved sheet '") + saved.name() + "' from '" + from
                + "'; '" + saved.name() + "' is now the active sheet");
        return CommandResult.CONTINUE;
    }
}
