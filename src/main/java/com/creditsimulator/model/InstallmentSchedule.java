package com.creditsimulator.model;

import java.util.List;
import java.util.Objects;

/** The calculated installments for every year of a loan, together with the inputs they came from. */
public record InstallmentSchedule(LoanApplication application, List<YearlyInstallment> years) {

    public InstallmentSchedule {
        Objects.requireNonNull(application, "application");
        years = List.copyOf(years);
    }
}
