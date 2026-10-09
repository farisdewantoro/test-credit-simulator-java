package com.creditsimulator.service;

import com.creditsimulator.model.LoanDraft;

/** Source of an existing calculation for the {@code load} command. */
public interface ExistingCalculationClient {

    /**
     * Fetches the loan inputs of an existing calculation. Only the format of each value is checked;
     * business rules are left to {@link LoanValidator}, like for typed input.
     *
     * @throws com.creditsimulator.exception.RemoteServiceException if the service fails or the payload is unusable
     */
    LoanDraft fetch();
}
