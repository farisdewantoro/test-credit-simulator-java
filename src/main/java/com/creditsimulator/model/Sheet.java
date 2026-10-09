package com.creditsimulator.model;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * One named calculation, like a spreadsheet tab: the loan inputs plus the last result calculated from
 * them. Any change to the inputs clears the result, so a sheet never shows a stale result.
 */
public final class Sheet {

    private final String name;
    private LoanDraft draft;
    private InstallmentSchedule lastResult;

    public Sheet(String name) {
        this(name, new LoanDraft(), null);
    }

    private Sheet(String name, LoanDraft draft, InstallmentSchedule lastResult) {
        this.name = Objects.requireNonNull(name, "name");
        this.draft = draft;
        this.lastResult = lastResult;
    }

    public String name() {
        return name;
    }

    /** A copy of the inputs; change them with {@link #edit} or {@link #replaceDraft}. */
    public LoanDraft draft() {
        return draft.copy();
    }

    public void edit(Consumer<LoanDraft> change) {
        change.accept(draft);
        lastResult = null;
    }

    public void replaceDraft(LoanDraft newDraft) {
        draft = newDraft.copy();
        lastResult = null;
    }

    public Optional<InstallmentSchedule> lastResult() {
        return Optional.ofNullable(lastResult);
    }

    public void recordResult(InstallmentSchedule result) {
        lastResult = Objects.requireNonNull(result, "result");
    }

    /** A deep copy of the inputs and result under a new name; the schedule itself is immutable. */
    public Sheet copyAs(String newName) {
        return new Sheet(newName, draft.copy(), lastResult);
    }
}
