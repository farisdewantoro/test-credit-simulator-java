package com.creditsimulator.policy;

import java.math.BigDecimal;

/** The smallest down payment accepted for a loan. */
public interface DownPaymentPolicy {

    BigDecimal minimumDownPayment(BigDecimal loanAmount);

    /** Human-readable form of the rule for error messages, e.g. {@code 35%}. */
    String describe();
}
