package com.creditsimulator.controller.command.impl;

import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleType;

public final class SetVehicleTypeCommand extends SetFieldCommand<VehicleType> {

    public SetVehicleTypeCommand() {
        super("jenis", "jenis <Mobil|Motor>", "Set the vehicle type", "Vehicle type",
                "Jenis Kendaraan (Motor|Mobil): ");
    }

    @Override
    public VehicleType parse(String text) {
        return VehicleType.parse(text);
    }

    @Override
    public void apply(LoanDraft draft, VehicleType value) {
        draft.vehicleType(value);
    }
}
