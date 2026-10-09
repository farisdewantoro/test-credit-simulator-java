package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanDraft;

public final class SetYearCommand extends SetFieldCommand<Integer> {

    public SetYearCommand() {
        super("tahun", "tahun <yyyy>", "Set the vehicle year (4 digits)", "Vehicle year",
                "Tahun Kendaraan (4 digit): ");
    }

    @Override
    public Integer parse(String text) {
        return InputParser.parseYear(text);
    }

    @Override
    public void apply(LoanDraft draft, Integer value) {
        draft.vehicleYear(value);
    }
}
