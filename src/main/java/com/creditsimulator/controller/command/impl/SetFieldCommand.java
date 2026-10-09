package com.creditsimulator.controller.command.impl;

import com.creditsimulator.controller.CommandContext;
import com.creditsimulator.controller.command.AbstractCommand;
import com.creditsimulator.controller.command.CommandResult;
import com.creditsimulator.model.LoanDraft;

import java.util.List;

/**
 * Sets one loan input on the active sheet. Only the value's own format is checked here; rules that
 * involve other fields run on {@code calculate}, so fields can be entered in any order.
 *
 * @param <T> the parsed type of the field
 */
public abstract class SetFieldCommand<T> extends AbstractCommand {

    private final String fieldLabel;
    private final String prompt;

    protected SetFieldCommand(String name, String usage, String description, String fieldLabel, String prompt) {
        super(name, usage, description, 1);
        this.fieldLabel = fieldLabel;
        this.prompt = prompt;
    }

    /** @throws com.creditsimulator.exception.InvalidInputException if the text has the wrong format */
    public abstract T parse(String text);

    public abstract void apply(LoanDraft draft, T value);

    /** The question the guided {@code new} flow asks for this field. */
    public String prompt() {
        return prompt;
    }

    protected String display(T value) {
        return String.valueOf(value);
    }

    @Override
    protected final CommandResult run(CommandContext context, List<String> args) {
        T value = parse(args.get(0));
        context.workspace().active().edit(draft -> apply(draft, value));
        context.view().info(fieldLabel + " set to " + display(value));
        return CommandResult.CONTINUE;
    }
}
