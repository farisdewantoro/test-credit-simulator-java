package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.view.ConsoleFormatter;

import java.math.BigDecimal;

public final class SetDownPaymentCommand extends SetFieldCommand<BigDecimal> {

    public SetDownPaymentCommand() {
        super("dp", "dp <amount>", "Set the down payment in rupiah (Baru >= 35%, Bekas >= 25% of loan)",
                "Down payment", "Jumlah DP: ");
    }

    @Override
    public BigDecimal parse(String text) {
        return InputParser.parseAmount(text, "DP");
    }

    @Override
    public void apply(LoanDraft draft, BigDecimal value) {
        draft.downPayment(value);
    }

    @Override
    protected String display(BigDecimal value) {
        return ConsoleFormatter.money(value);
    }
}
