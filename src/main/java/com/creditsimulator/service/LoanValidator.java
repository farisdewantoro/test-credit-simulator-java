package com.creditsimulator.service;

import com.creditsimulator.exception.ValidationException;
import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.policy.DownPaymentPolicy;
import com.creditsimulator.policy.VehiclePolicyFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.creditsimulator.view.ConsoleFormatter.money;

/**
 * Checks the rules that span several fields and turns a complete {@link LoanDraft} into a
 * {@link LoanApplication}. Reports every broken rule at once instead of stopping at the first.
 */
public final class LoanValidator {

    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000");

    private final VehiclePolicyFactory policies;
    private final Clock clock;

    public LoanValidator(VehiclePolicyFactory policies, Clock clock) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public LoanApplication validate(LoanDraft draft) {
        List<String> violations = new ArrayList<>();
        List<String> missing = draft.missingFields();
        if (!missing.isEmpty()) {
            violations.add("Missing: " + String.join(", ", missing));
        }

        draft.vehicleYear().ifPresent(year -> checkYear(year, draft.condition().orElse(null), violations));
        draft.tenor().ifPresent(tenor -> checkTenor(tenor, violations));
        draft.loanAmount().ifPresent(loan -> checkLoanAmount(loan, violations));
        draft.downPayment().ifPresent(downPayment ->
                checkDownPayment(downPayment, draft.loanAmount().orElse(null), draft.condition().orElse(null),
                        violations));

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
        return new LoanApplication(
                draft.vehicleType().orElseThrow(),
                draft.condition().orElseThrow(),
                draft.vehicleYear().orElseThrow(),
                draft.loanAmount().orElseThrow(),
                draft.tenor().orElseThrow(),
                draft.downPayment().orElseThrow());
    }

    private void checkYear(int year, VehicleCondition condition, List<String> violations) {
        int currentYear = Year.now(clock).getValue();
        if (year < 1000 || year > 9999) {
            violations.add("Vehicle year must be a 4-digit number");
        } else if (year > currentYear + 1) {
            violations.add("Vehicle year cannot be later than " + (currentYear + 1));
        } else if (condition == VehicleCondition.BARU && year < currentYear - 1) {
            violations.add("A new (Baru) vehicle cannot be older than " + (currentYear - 1) + ", got " + year);
        }
    }

    private static void checkTenor(int tenor, List<String> violations) {
        if (tenor < InputParser.MIN_TENOR || tenor > InputParser.MAX_TENOR) {
            violations.add("Tenor must be between " + InputParser.MIN_TENOR + " and " + InputParser.MAX_TENOR
                    + " years");
        }
    }

    private static void checkLoanAmount(BigDecimal loan, List<String> violations) {
        if (loan.signum() <= 0) {
            violations.add("Loan amount must be greater than 0");
        } else if (loan.compareTo(MAX_LOAN_AMOUNT) > 0) {
            violations.add("Loan amount cannot exceed " + money(MAX_LOAN_AMOUNT));
        }
    }

    private void checkDownPayment(BigDecimal downPayment, BigDecimal loan, VehicleCondition condition,
                                  List<String> violations) {
        if (downPayment.signum() <= 0) {
            violations.add("DP must be greater than 0");
            return;
        }
        if (loan == null || loan.signum() <= 0) {
            return;
        }
        if (downPayment.compareTo(loan) >= 0) {
            violations.add("DP must be less than the loan amount (" + money(loan) + ")");
            return;
        }
        if (condition != null) {
            DownPaymentPolicy policy = policies.downPayment(condition);
            BigDecimal minimum = policy.minimumDownPayment(loan);
            if (downPayment.compareTo(minimum) < 0) {
                violations.add("DP must be at least " + policy.describe() + " of loan amount for a "
                        + condition.label() + " vehicle (min " + money(minimum) + ")");
            }
        }
    }
}
