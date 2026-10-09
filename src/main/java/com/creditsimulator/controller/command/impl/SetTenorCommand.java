package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanDraft;

public final class SetTenorCommand extends SetFieldCommand<Integer> {

    public SetTenorCommand() {
        super("tenor", "tenor <1-6>", "Set the loan tenor in years", "Tenor", "Tenor Pinjaman (1-6 thn): ");
    }

    @Override
    public Integer parse(String text) {
        return InputParser.parseTenor(text);
    }

    @Override
    public void apply(LoanDraft draft, Integer value) {
        draft.tenor(value);
    }

    @Override
    protected String display(Integer value) {
        return value + " thn";
    }
}
