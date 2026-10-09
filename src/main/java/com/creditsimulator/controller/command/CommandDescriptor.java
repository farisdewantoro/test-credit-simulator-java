package com.creditsimulator.controller.command;

/**
 * What {@code show} prints for a command.
 *
 * @param name        the word that runs the command, e.g. {@code tahun}
 * @param usage       how to call it, e.g. {@code tahun <yyyy>}
 * @param description what it does
 */
public record CommandDescriptor(String name, String usage, String description) {
}
