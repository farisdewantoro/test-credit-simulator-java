package com.creditsimulator.exception;

/** The calculation web service could not be reached or sent something unusable. */
public class RemoteServiceException extends CreditSimulatorException {

    private static final long serialVersionUID = 1L;

    public RemoteServiceException(String message) {
        super(message);
    }

    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
