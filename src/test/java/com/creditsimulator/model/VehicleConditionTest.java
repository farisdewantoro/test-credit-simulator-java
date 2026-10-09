package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleConditionTest {

    @ParameterizedTest
    @ValueSource(strings = {"Baru", "BARU", "baru", " Baru "})
    void parsesBaru(String input) {
        assertEquals(VehicleCondition.BARU, VehicleCondition.parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bekas", "BEKAS", "bekas"})
    void parsesBekas(String input) {
        assertEquals(VehicleCondition.BEKAS, VehicleCondition.parse(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"new", "lama", "2", "Bekas!"})
    void rejectsAnythingElse(String input) {
        InvalidInputException error = assertThrows(InvalidInputException.class, () -> VehicleCondition.parse(input));
        assertEquals("Vehicle condition must be Baru or Bekas", error.getMessage());
    }
}
