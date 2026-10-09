package com.creditsimulator.exception;

/**
 * Base type for every error caused by user input or the environment. The controller reports these to
 * the user and keeps the session running; anything else is treated as a bug.
 */
public class CreditSimulatorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CreditSimulatorException(String message) {
        super(message);
    }

    public CreditSimulatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
