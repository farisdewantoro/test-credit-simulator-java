package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.Sheet;
import com.creditsimulator.view.ConsoleFormatter;

import java.util.List;

/**
 * Fetches an existing calculation from the web service into the active sheet, then calculates and shows
 * it. If the loaded data breaks a rule it stays in the sheet so the user can fix it, and the violations
 * are reported.
 */
public final class LoadCommand extends AbstractCommand {

    public LoadCommand() {
        super("load", "load", "Load an existing calculation from the web service and calculate it", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        LoanDraft loaded = context.client().fetch();
        Sheet sheet = context.workspace().active();
        sheet.replaceDraft(loaded);
        context.view().info("Loaded into sheet '" + sheet.name() + "': " + summary(loaded));
        CalculateCommand.calculateActiveSheet(context);
        return CommandResult.CONTINUE;
    }

    private static String summary(LoanDraft draft) {
        return draft.vehicleType().orElseThrow() + " " + draft.condition().orElseThrow() + " "
                + draft.vehicleYear().orElseThrow()
                + ", loan " + ConsoleFormatter.money(draft.loanAmount().orElseThrow())
                + ", tenor " + draft.tenor().orElseThrow() + " thn"
                + ", DP " + ConsoleFormatter.money(draft.downPayment().orElseThrow());
    }
}
