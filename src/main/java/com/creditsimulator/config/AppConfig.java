package com.creditsimulator.config;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime settings, read once at startup.
 *
 * @param clock source of "now", so the current year can be fixed in tests
 * @param debug print stack traces for unexpected errors ({@code CREDIT_SIMULATOR_DEBUG=1})
 */
public record AppConfig(Clock clock, boolean debug) {

    public static final String DEBUG_ENV = "CREDIT_SIMULATOR_DEBUG";

    public AppConfig {
        Objects.requireNonNull(clock, "clock");
    }

    public static AppConfig fromEnvironment(Map<String, String> env) {
        return new AppConfig(Clock.systemDefaultZone(), "1".equals(env.get(DEBUG_ENV)));
    }
}
