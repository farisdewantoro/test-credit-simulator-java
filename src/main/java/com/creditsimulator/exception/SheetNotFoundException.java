package com.creditsimulator.exception;

import java.util.List;

/** {@code switch_sheet} named a sheet that does not exist. */
public class SheetNotFoundException extends CreditSimulatorException {

    private static final long serialVersionUID = 1L;

    public SheetNotFoundException(String name, List<String> available) {
        super("Sheet '" + name + "' not found. Available sheets: " + String.join(", ", available));
    }
}
