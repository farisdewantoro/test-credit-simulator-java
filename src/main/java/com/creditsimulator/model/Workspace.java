package com.creditsimulator.model;

import com.creditsimulator.exception.InvalidInputException;
import com.creditsimulator.exception.SheetNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * All sheets of a session and which one is active. Starts with a single sheet named {@code default}.
 * Names are matched case-insensitively and shown as typed. Sheets live in memory for one session.
 */
public final class Workspace {

    public static final String DEFAULT_SHEET = "default";

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

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

    public boolean contains(String name) {
        return sheets.containsKey(key(name));
    }

    /**
     * "Save as": copies the active sheet's inputs and result to {@code name}, replacing any sheet with
     * that name, and makes the copy active. The sheet it was copied from is left unchanged.
     */
    public Sheet saveAs(String name) {
        String trimmed = validName(name);
        Sheet copy = active.copyAs(trimmed);
        sheets.put(key(trimmed), copy);
        active = copy;
        return copy;
    }

    public Sheet switchTo(String name) {
        Sheet sheet = sheets.get(key(name));
        if (sheet == null) {
            throw new SheetNotFoundException(name, sheets.values().stream().map(Sheet::name).toList());
        }
        active = sheet;
        return sheet;
    }

    private static String validName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (!NAME.matcher(trimmed).matches()) {
            throw new InvalidInputException("Sheet name must be 1-32 characters: letters, digits, '-' or '_'");
        }
        return trimmed;
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
