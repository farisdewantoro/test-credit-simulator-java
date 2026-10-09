package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.view.ConsoleFormatter;

import java.math.BigDecimal;

public final class SetLoanAmountCommand extends SetFieldCommand<BigDecimal> {

    public SetLoanAmountCommand() {
        super("nominal", "nominal <amount>", "Set the total loan amount in rupiah (max 1000000000)", "Loan amount",
                "Jumlah Pinjaman Total (maks 1000000000): ");
    }

    @Override
    public BigDecimal parse(String text) {
        return InputParser.parseAmount(text, "Loan amount");
    }

    @Override
    public void apply(LoanDraft draft, BigDecimal value) {
        draft.loanAmount(value);
    }

    @Override
    protected String display(BigDecimal value) {
        return ConsoleFormatter.money(value);
    }
}
