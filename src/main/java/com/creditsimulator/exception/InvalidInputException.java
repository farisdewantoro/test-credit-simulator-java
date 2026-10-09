package com.creditsimulator.exception;

/** A single value has the wrong format, e.g. {@code tahun 20a6} or {@code jenis truk}. */
public class InvalidInputException extends CreditSimulatorException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
