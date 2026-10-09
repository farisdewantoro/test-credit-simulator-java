package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.exception.InvalidInputException;
import com.creditsimulator.io.InputSource;
import com.creditsimulator.model.LoanDraft;

import java.util.List;
import java.util.Optional;

/**
 * The guided flow from the spec: asks for type → condition → year → loan → tenor → DP in order, then
 * calculates. Each answer is parsed by the matching set command, so the rules are identical. When
 * interactive an invalid answer is asked again; from a file it aborts the flow so the following lines
 * are not misread as answers. The active sheet only changes once every answer is in.
 */
public final class NewCalculationCommand extends AbstractCommand {

    private final List<SetFieldCommand<?>> fields = List.of(
            new SetVehicleTypeCommand(),
            new SetConditionCommand(),
            new SetYearCommand(),
            new SetLoanAmountCommand(),
            new SetTenorCommand(),
            new SetDownPaymentCommand());

    public NewCalculationCommand() {
        super("new", "new", "Guided input: asks for every field in order, then calculates", 0);
    }

    @Override
    protected CommandResult run(CommandContext context, List<String> args) {
        LoanDraft draft = new LoanDraft();
        for (SetFieldCommand<?> field : fields) {
            ask(context, field, draft);
        }
        context.workspace().active().replaceDraft(draft);
        CalculateCommand.calculateActiveSheet(context);
        return CommandResult.CONTINUE;
    }

    private static <T> void ask(CommandContext context, SetFieldCommand<T> field, LoanDraft draft) {
        InputSource input = context.input();
        while (true) {
            Optional<String> answer = input.next(field.prompt());
            if (answer.isEmpty()) {
                throw new InvalidInputException("Input ended before 'new' was finished; nothing was changed");
            }
            try {
                field.apply(draft, field.parse(answer.get()));
                return;
            } catch (InvalidInputException e) {
                if (!input.interactive()) {
                    throw new InvalidInputException(e.getMessage() + "; 'new' was aborted and nothing was changed");
                }
                context.view().error(e.getMessage() + ". Please try again.");
            }
        }
    }
}
