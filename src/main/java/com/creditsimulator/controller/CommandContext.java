package com.creditsimulator.controller;

import com.creditsimulator.io.InputSource;
import com.creditsimulator.model.Workspace;
import com.creditsimulator.service.InstallmentCalculator;
import com.creditsimulator.service.LoanValidator;
import com.creditsimulator.view.ConsoleView;

import java.util.Objects;

/** Everything a command is allowed to use. */
public record CommandContext(
        Workspace workspace,
        ConsoleView view,
        InputSource input,
        LoanValidator validator,
        InstallmentCalculator calculator) {

    public CommandContext {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(validator, "validator");
        Objects.requireNonNull(calculator, "calculator");
    }
}
