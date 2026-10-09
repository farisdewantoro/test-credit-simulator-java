package com.creditsimulator.service;

import com.creditsimulator.exception.RemoteServiceException;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs against a real in-process HTTP server from the JDK, so no mocking library is needed. */
class HttpExistingCalculationClientTest {

    private static final String VALID_JSON = """
            {
              "vehicleType": "Mobil",
              "vehicleCondition": "Baru",
              "vehicleYear": 2025,
              "totalLoanAmount": 1000000000,
              "loanTenure": 6,
              "downPayment": 500000000
            }""";

    private HttpServer server;
    private ExecutorService executor;
    private final AtomicReference<String> acceptHeader = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void mapsAValidResponseToADraft() {
        respond(200, VALID_JSON, 0);

        LoanDraft draft = client(Duration.ofSeconds(5)).fetch();

        assertEquals(VehicleType.MOBIL, draft.vehicleType().orElseThrow());
        assertEquals(VehicleCondition.BARU, draft.condition().orElseThrow());
        assertEquals(2025, draft.vehicleYear().orElseThrow());
        assertEquals(new BigDecimal("1000000000"), draft.loanAmount().orElseThrow());
        assertEquals(6, draft.tenor().orElseThrow());
        assertEquals(new BigDecimal("500000000"), draft.downPayment().orElseThrow());
        assertEquals("application/json", acceptHeader.get());
    }

    @Test
    void ignoresUnknownPropertiesAndParsesValuesCaseInsensitively() {
        respond(200, """
                {"vehicleType":"MOTOR","vehicleCondition":"bekas","vehicleYear":2019,"totalLoanAmount":2.0E7,
                 "loanTenure":1,"downPayment":5000000,"currency":"IDR","extra":{"nested":true}}""", 0);

        LoanDraft draft = client(Duration.ofSeconds(5)).fetch();

        assertEquals(VehicleType.MOTOR, draft.vehicleType().orElseThrow());
        assertEquals(VehicleCondition.BEKAS, draft.condition().orElseThrow());
        assertEquals(new BigDecimal("20000000"), draft.loanAmount().orElseThrow());
    }

    @Test
    void leavesBusinessRulesToTheValidator() {
        respond(200, VALID_JSON.replace("\"loanTenure\": 6", "\"loanTenure\": 9"), 0);

        assertEquals(9, client(Duration.ofSeconds(5)).fetch().tenor().orElseThrow());
    }

    @Test
    void reportsServerErrors() {
        respond(500, "oops", 0);

        assertEquals("Calculation service returned HTTP 500", failure().getMessage());
    }

    @Test
    void reportsMalformedJson() {
        respond(200, "{\"vehicleType\": ", 0);

        assertTrue(failure().getMessage().startsWith("Calculation service returned an invalid payload: "));
    }

    @Test
    void reportsMissingProperties() {
        respond(200, VALID_JSON.replace("\"loanTenure\": 6,", ""), 0);

        assertEquals("Calculation service returned an invalid payload: missing 'loanTenure'", failure().getMessage());
    }

    @Test
    void reportsValuesWithTheWrongFormat() {
        respond(200, VALID_JSON.replace("\"Mobil\"", "\"Truk\""), 0);

        assertEquals("Calculation service returned an invalid payload: Vehicle type must be Mobil or Motor",
                failure().getMessage());
    }

    @Test
    void reportsAnEmptyBody() {
        respond(200, "null", 0);

        assertEquals("Calculation service returned an invalid payload: empty body", failure().getMessage());
    }

    @Test
    void timesOutOnASlowResponse() {
        respond(200, VALID_JSON, 2_000);

        RemoteServiceException error = assertThrows(RemoteServiceException.class,
                () -> client(Duration.ofMillis(200)).fetch());

        assertEquals("Could not reach calculation service (timeout after 200ms)", error.getMessage());
    }

    @Test
    void reportsAnUnreachableServer() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = socket.getLocalPort();
        }
        URI url = URI.create("http://127.0.0.1:" + closedPort + "/calculation");
        HttpExistingCalculationClient client =
                new HttpExistingCalculationClient(url, Duration.ofSeconds(2), Duration.ofSeconds(2));

        RemoteServiceException error = assertThrows(RemoteServiceException.class, client::fetch);

        assertEquals("Could not reach calculation service: connection refused (check the URL and that the service is up)",
                error.getMessage());
    }

    private RemoteServiceException failure() {
        return assertThrows(RemoteServiceException.class, () -> client(Duration.ofSeconds(5)).fetch());
    }

    private HttpExistingCalculationClient client(Duration requestTimeout) {
        URI url = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/calculation");
        return new HttpExistingCalculationClient(url, Duration.ofSeconds(2), requestTimeout);
    }

    private void respond(int status, String body, long delayMillis) {
        server.createContext("/calculation", exchange -> {
            acceptHeader.set(exchange.getRequestHeaders().getFirst("Accept"));
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
                return;
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }
}
