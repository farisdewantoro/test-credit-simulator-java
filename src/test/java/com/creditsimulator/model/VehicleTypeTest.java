package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"Mobil", "MOBIL", "mobil", " Mobil ", "mObIl"})
    void parsesMobilIgnoringCaseAndSpaces(String input) {
        assertEquals(VehicleType.MOBIL, VehicleType.parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Motor", "MOTOR", "motor"})
    void parsesMotor(String input) {
        assertEquals(VehicleType.MOTOR, VehicleType.parse(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"car", "truk", "123", "Mobil1", "  "})
    void rejectsAnythingElse(String input) {
        InvalidInputException error = assertThrows(InvalidInputException.class, () -> VehicleType.parse(input));
        assertEquals("Vehicle type must be Mobil or Motor", error.getMessage());
    }

    @org.junit.jupiter.api.Test
    void displaysItsLabel() {
        assertEquals("Mobil", VehicleType.MOBIL.toString());
        assertEquals("Motor", VehicleType.MOTOR.label());
    }
}
