package com.creditsimulator.controller.command;

import com.creditsimulator.controller.CommandContext;

import java.util.List;

/** One user command. Adding a command means adding one implementation and registering it. */
public interface Command {

    CommandDescriptor descriptor();

    /**
     * @param args the words after the command name
     * @throws com.creditsimulator.exception.CreditSimulatorException for anything the user can fix
     */
    CommandResult execute(CommandContext context, List<String> args);
}
