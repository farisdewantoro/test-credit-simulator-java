package com.creditsimulator.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The loan inputs while the user is still editing them. Every field is optional so they can be entered
 * in any order; {@code LoanValidator} turns a complete draft into a {@link LoanApplication}.
 */
public final class LoanDraft {

    private VehicleType vehicleType;
    private VehicleCondition condition;
    private Integer vehicleYear;
    private BigDecimal loanAmount;
    private Integer tenor;
    private BigDecimal downPayment;

    public LoanDraft() {
    }

    public LoanDraft copy() {
        LoanDraft copy = new LoanDraft();
        copy.vehicleType = vehicleType;
        copy.condition = condition;
        copy.vehicleYear = vehicleYear;
        copy.loanAmount = loanAmount;
        copy.tenor = tenor;
        copy.downPayment = downPayment;
        return copy;
    }

    public Optional<VehicleType> vehicleType() {
        return Optional.ofNullable(vehicleType);
    }

    public LoanDraft vehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
        return this;
    }

    public Optional<VehicleCondition> condition() {
        return Optional.ofNullable(condition);
    }

    public LoanDraft condition(VehicleCondition condition) {
        this.condition = condition;
        return this;
    }

    public Optional<Integer> vehicleYear() {
        return Optional.ofNullable(vehicleYear);
    }

    public LoanDraft vehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
        return this;
    }

    public Optional<BigDecimal> loanAmount() {
        return Optional.ofNullable(loanAmount);
    }

    public LoanDraft loanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
        return this;
    }

    public Optional<Integer> tenor() {
        return Optional.ofNullable(tenor);
    }

    public LoanDraft tenor(Integer tenor) {
        this.tenor = tenor;
        return this;
    }

    public Optional<BigDecimal> downPayment() {
        return Optional.ofNullable(downPayment);
    }

    public LoanDraft downPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
        return this;
    }

    /** Names of the commands that still need to be run, in input order, e.g. {@code [tenor, dp]}. */
    public List<String> missingFields() {
        List<String> missing = new ArrayList<>();
        if (vehicleType == null) {
            missing.add("jenis");
        }
        if (condition == null) {
            missing.add("kondisi");
        }
        if (vehicleYear == null) {
            missing.add("tahun");
        }
        if (loanAmount == null) {
            missing.add("nominal");
        }
        if (tenor == null) {
            missing.add("tenor");
        }
        if (downPayment == null) {
            missing.add("dp");
        }
        return Collections.unmodifiableList(missing);
    }
}
