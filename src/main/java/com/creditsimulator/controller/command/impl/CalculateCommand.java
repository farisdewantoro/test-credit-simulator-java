package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.model.InstallmentSchedule;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.Sheet;

import java.util.List;

public final class CalculateCommand extends AbstractCommand {

    public CalculateCommand() {
        super("calculate", "calculate", "Validate the inputs and show the monthly installment for each year", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        calculateActiveSheet(context);
        return CommandResult.CONTINUE;
    }

    /**
     * Validates the active sheet, calculates it, stores the result on the sheet and shows it. Shared by
     * every command that ends in a calculation.
     *
     * @throws com.creditsimulator.exception.ValidationException if a rule is broken; nothing is stored
     */
    static void calculateActiveSheet(CommandContext context) {
        Sheet sheet = context.workspace().active();
        LoanApplication application = context.validator().validate(sheet.draft());
        InstallmentSchedule schedule = context.calculator().calculate(application);
        sheet.recordResult(schedule);
        context.view().schedule(schedule);
    }
}
