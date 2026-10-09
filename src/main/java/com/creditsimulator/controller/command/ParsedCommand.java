package com.creditsimulator.controller.command;

import java.util.List;

/** A command line split into the command to run and its arguments. */
public record ParsedCommand(Command command, List<String> args) {

    public ParsedCommand {
        args = List.copyOf(args);
    }
}
