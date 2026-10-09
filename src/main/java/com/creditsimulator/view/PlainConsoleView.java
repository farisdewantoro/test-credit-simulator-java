package com.creditsimulator.view;

import com.creditsimulator.controller.command.CommandDescriptor;
import com.creditsimulator.model.InstallmentSchedule;
import com.creditsimulator.model.LoanApplication;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.Sheet;
import com.creditsimulator.model.YearlyInstallment;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.creditsimulator.view.ConsoleFormatter.money;
import static com.creditsimulator.view.ConsoleFormatter.rate;

/** Plain-text view: normal output to stdout, errors to stderr. */
public final class PlainConsoleView implements ConsoleView {

    private static final String UNSET = "-";

    private final PrintStream out;
    private final PrintStream err;
    private final boolean debug;

    public PlainConsoleView(PrintStream out, PrintStream err, boolean debug) {
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
        this.debug = debug;
    }

    @Override
    public void info(String message) {
        out.println(message);
        out.flush();
    }

    @Override
    public void error(String message) {
        out.flush();
        err.println("Error: " + message);
        err.flush();
    }

    @Override
    public void unexpectedError(String locationPrefix, Throwable error) {
        out.flush();
        err.println("Unexpected error: " + locationPrefix + error);
        if (debug) {
            error.printStackTrace(err);
        } else {
            err.println("(set CREDIT_SIMULATOR_DEBUG=1 to see the stack trace)");
        }
        err.flush();
    }

    @Override
    public void echo(String line) {
        out.println(line);
    }

    @Override
    public void schedule(InstallmentSchedule schedule) {
        LoanApplication application = schedule.application();
        out.println("Monthly installments for " + application.vehicleType() + " " + application.condition() + " "
                + application.vehicleYear() + ", principal " + money(application.principal()) + ":");
        printYears(schedule, "");
        out.flush();
    }

    @Override
    public void status(Sheet sheet) {
        LoanDraft draft = sheet.draft();
        out.println("Sheet '" + sheet.name() + "'");
        out.println("  jenis   : " + text(draft.vehicleType()));
        out.println("  kondisi : " + text(draft.condition()));
        out.println("  tahun   : " + text(draft.vehicleYear()));
        out.println("  nominal : " + draft.loanAmount().map(ConsoleFormatter::money).orElse(UNSET));
        out.println("  tenor   : " + draft.tenor().map(tenor -> tenor + " thn").orElse(UNSET));
        out.println("  dp      : " + draft.downPayment().map(ConsoleFormatter::money).orElse(UNSET));
        sheet.lastResult().ifPresentOrElse(
                result -> {
                    out.println("  result  :");
                    printYears(result, "    ");
                },
                () -> out.println("  result  : not calculated"));
        out.flush();
    }

    @Override
    public void sheets(List<Sheet> sheets, Sheet active) {
        List<String[]> rows = sheets.stream().map(PlainConsoleView::summaryColumns).toList();
        int[] widths = new int[rows.isEmpty() ? 0 : rows.get(0).length];
        rows.forEach(row -> {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        });
        for (int r = 0; r < rows.size(); r++) {
            String[] row = rows.get(r);
            StringBuilder line = new StringBuilder(sheets.get(r) == active ? "* " : "  ");
            for (int i = 0; i < row.length; i++) {
                line.append(i == row.length - 1 ? row[i] : pad(row[i], widths[i]) + "  ");
            }
            out.println(line);
        }
        out.flush();
    }

    private static String[] summaryColumns(Sheet sheet) {
        LoanDraft draft = sheet.draft();
        return new String[]{
                sheet.name(),
                text(draft.vehicleType()) + "/" + text(draft.condition()) + "/" + text(draft.vehicleYear()),
                draft.loanAmount().map(ConsoleFormatter::money).orElse(UNSET),
                draft.tenor().map(tenor -> tenor + " thn").orElse(UNSET),
                sheet.lastResult().isPresent() ? "✔ calculated" : "not calculated"};
    }

    @Override
    public void commands(List<CommandDescriptor> commands) {
        int usageWidth = commands.stream().mapToInt(command -> command.usage().length()).max().orElse(0);
        out.println("Available commands:");
        commands.forEach(command -> out.println("  " + pad(command.usage(), usageWidth) + "  " + command.description()));
        out.flush();
    }

    private void printYears(InstallmentSchedule schedule, String indent) {
        for (YearlyInstallment year : schedule.years()) {
            out.println(indent + "tahun " + year.year() + " : " + money(year.monthly()) + "/bln , Suku Bunga : "
                    + rate(year.interestRate()));
        }
    }

    private static String text(Optional<?> value) {
        return value.map(String::valueOf).orElse(UNSET);
    }

    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}
