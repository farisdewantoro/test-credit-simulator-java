package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;

import java.util.Arrays;

/** Vehicle type, parsed case-insensitively from {@code Mobil} or {@code Motor}. */
public enum VehicleType {
    MOBIL("Mobil"),
    MOTOR("Motor");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static VehicleType parse(String text) {
        String value = text == null ? "" : text.trim();
        return Arrays.stream(values())
                .filter(type -> type.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("Vehicle type must be Mobil or Motor"));
    }

    @Override
    public String toString() {
        return label;
    }
}
