package com.creditsimulator.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** All sheets of a session and which one is active. Starts with a single sheet named {@code default}. */
public final class Workspace {

    public static final String DEFAULT_SHEET = "default";

    private final Map<String, Sheet> sheets = new LinkedHashMap<>();
    private Sheet active;

    public Workspace() {
        active = new Sheet(DEFAULT_SHEET);
        sheets.put(key(DEFAULT_SHEET), active);
    }

    public Sheet active() {
        return active;
    }

    public List<Sheet> list() {
        return new ArrayList<>(sheets.values());
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
