package com.creditsimulator.config;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime settings, read once at startup.
 *
 * @param loadUrl        web service used by {@code load} ({@code CREDIT_SIMULATOR_LOAD_URL} overrides it)
 * @param connectTimeout how long to wait for the connection
 * @param requestTimeout how long to wait for the whole response
 * @param clock          source of "now", so the current year can be fixed in tests
 * @param debug          print stack traces for unexpected errors ({@code CREDIT_SIMULATOR_DEBUG=1})
 */
public record AppConfig(URI loadUrl, Duration connectTimeout, Duration requestTimeout, Clock clock, boolean debug) {

    public static final String LOAD_URL_ENV = "CREDIT_SIMULATOR_LOAD_URL";
    public static final String DEBUG_ENV = "CREDIT_SIMULATOR_DEBUG";
    public static final URI DEFAULT_LOAD_URL =
            URI.create("https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c");
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);

    public AppConfig {
        Objects.requireNonNull(loadUrl, "loadUrl");
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(requestTimeout, "requestTimeout");
        Objects.requireNonNull(clock, "clock");
    }

    /** Defaults with the given clock; handy for tests. */
    public static AppConfig withClock(Clock clock) {
        return new AppConfig(DEFAULT_LOAD_URL, DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, clock, false);
    }

    /** @throws IllegalArgumentException if {@code CREDIT_SIMULATOR_LOAD_URL} is not an http(s) URL */
    public static AppConfig fromEnvironment(Map<String, String> env) {
        String override = env.get(LOAD_URL_ENV);
        URI loadUrl = override == null || override.isBlank() ? DEFAULT_LOAD_URL : parseUrl(override.trim());
        return new AppConfig(loadUrl, DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, Clock.systemDefaultZone(),
                "1".equals(env.get(DEBUG_ENV)));
    }

    private static URI parseUrl(String text) {
        URI uri;
        try {
            uri = URI.create(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(LOAD_URL_ENV + " is not a valid URL: " + text, e);
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null) {
            throw new IllegalArgumentException(LOAD_URL_ENV + " must be an http or https URL: " + text);
        }
        return uri;
    }
}
