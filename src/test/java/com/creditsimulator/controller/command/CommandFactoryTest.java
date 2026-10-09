package com.creditsimulator.controller.command;

import com.creditsimulator.controller.command.impl.SetYearCommand;
import com.creditsimulator.exception.UnknownCommandException;
import com.creditsimulator.support.TestConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandFactoryTest {

    private final CommandFactory factory = CommandFactory.withDefaultCommands();

    @ParameterizedTest
    @ValueSource(strings = {"tahun 2022", "TAHUN 2022", "  Tahun   2022  "})
    void looksUpCommandsIgnoringCaseAndExtraSpaces(String line) {
        ParsedCommand parsed = factory.parse(line);

        assertInstanceOf(SetYearCommand.class, parsed.command());
        assertEquals(List.of("2022"), parsed.args());
    }

    @Test
    void unknownCommandIsRejectedWithAHint() {
        UnknownCommandException error = assertThrows(UnknownCommandException.class, () -> factory.parse("foo bar"));

        assertEquals("Unknown command 'foo'. Type 'show' to list commands.", error.getMessage());
    }

    @Test
    void refusesDuplicateRegistration() {
        assertThrows(IllegalArgumentException.class, () -> factory.register(new SetYearCommand()));
    }

    @Test
    void showListsEveryRegisteredCommand() {
        TestConsole console = TestConsole.interactive("show");
        console.run();

        List<CommandDescriptor> descriptors = factory.descriptors();
        assertTrue(descriptors.size() >= 10, "expected the full command set");
        for (CommandDescriptor descriptor : descriptors) {
            assertTrue(console.out().contains(descriptor.usage()), "show is missing " + descriptor.name());
            assertTrue(console.out().contains(descriptor.description()), "show is missing " + descriptor.name());
        }
    }
}
