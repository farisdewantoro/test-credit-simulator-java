package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;

public final class SetConditionCommand extends SetFieldCommand<VehicleCondition> {

    public SetConditionCommand() {
        super("kondisi", "kondisi <Baru|Bekas>", "Set the vehicle condition (new or used)", "Vehicle condition",
                "Kendaraan (Baru|Bekas): ");
    }

    @Override
    public VehicleCondition parse(String text) {
        return VehicleCondition.parse(text);
    }

    @Override
    public void apply(LoanDraft draft, VehicleCondition value) {
        draft.condition(value);
    }
}
