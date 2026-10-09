package com.creditsimulator.exception;

import java.util.List;

/** One or more business rules are broken. Carries every violation, not just the first one. */
public class ValidationException extends CreditSimulatorException {

    private static final long serialVersionUID = 1L;

    private final List<String> violations;

    public ValidationException(List<String> violations) {
        super(buildMessage(violations));
        this.violations = List.copyOf(violations);
    }

    public List<String> violations() {
        return violations;
    }

    private static String buildMessage(List<String> violations) {
        if (violations.isEmpty()) {
            throw new IllegalArgumentException("violations must not be empty");
        }
        if (violations.size() == 1) {
            return violations.get(0);
        }
        StringBuilder message = new StringBuilder("Cannot calculate, " + violations.size() + " problems found:");
        violations.forEach(violation -> message.append(System.lineSeparator()).append("  - ").append(violation));
        return message.toString();
    }
}
