package com.creditsimulator.exception;

/** The first word of a line is not a registered command. */
public class UnknownCommandException extends CreditSimulatorException {

    private static final long serialVersionUID = 1L;

    public UnknownCommandException(String name) {
        super("Unknown command '" + name + "'. Type 'show' to list commands.");
    }
}
