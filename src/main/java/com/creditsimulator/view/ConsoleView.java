package com.creditsimulator.view;

import com.creditsimulator.controller.command.CommandDescriptor;
import com.creditsimulator.model.InstallmentSchedule;
import com.creditsimulator.model.Sheet;

import java.util.List;

/** Everything the application shows the user. Commands talk to this, never to a stream directly. */
public interface ConsoleView {

    void info(String message);

    void error(String message);

    /** A bug, not a user error. Shows the stack trace only in debug mode. */
    void unexpectedError(String locationPrefix, Throwable error);

    /** Repeats a command read from a file, so the output reads like a transcript. */
    void echo(String line);

    void schedule(InstallmentSchedule schedule);

    void status(Sheet sheet);

    void sheets(List<Sheet> sheets, Sheet active);

    void commands(List<CommandDescriptor> commands);
}
