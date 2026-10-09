package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;

import java.util.Arrays;

/** Vehicle condition, parsed case-insensitively from {@code Baru} (new) or {@code Bekas} (used). */
public enum VehicleCondition {
    BARU("Baru"),
    BEKAS("Bekas");

    private final String label;

    VehicleCondition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static VehicleCondition parse(String text) {
        String value = text == null ? "" : text.trim();
        return Arrays.stream(values())
                .filter(condition -> condition.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("Vehicle condition must be Baru or Bekas"));
    }

    @Override
    public String toString() {
        return label;
    }
}
