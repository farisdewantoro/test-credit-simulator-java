package com.creditsimulator.service;

import com.creditsimulator.exception.InvalidInputException;
import com.creditsimulator.exception.RemoteServiceException;
import com.creditsimulator.model.InputParser;
import com.creditsimulator.model.LoanDraft;
import com.creditsimulator.model.VehicleCondition;
import com.creditsimulator.model.VehicleType;
import com.creditsimulator.service.dto.ExistingCalculationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Calls the calculation web service with the JDK HTTP client and maps its JSON with Jackson. */
public final class HttpExistingCalculationClient implements ExistingCalculationClient {

    private static final String UNREACHABLE = "Could not reach calculation service";
    private static final String INVALID_PAYLOAD = "Calculation service returned an invalid payload: ";

    private final HttpClient http;
    private final URI url;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .build();

    public HttpExistingCalculationClient(URI url, Duration connectTimeout, Duration requestTimeout) {
        this.url = Objects.requireNonNull(url, "url");
        this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        this.http = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public LoanDraft fetch() {
        HttpResponse<String> response = get();
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new RemoteServiceException("Calculation service returned HTTP " + response.statusCode());
        }
        return toDraft(parse(response.body()));
    }

    private HttpResponse<String> get() {
        HttpRequest request = HttpRequest.newBuilder(url)
                .GET()
                .header("Accept", "application/json")
                .timeout(requestTimeout)
                .build();
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpConnectTimeoutException e) {
            throw new RemoteServiceException(UNREACHABLE + " (connect timeout after " + seconds(connectTimeout) + ")", e);
        } catch (HttpTimeoutException e) {
            throw new RemoteServiceException(UNREACHABLE + " (timeout after " + seconds(requestTimeout) + ")", e);
        } catch (IOException e) {
            throw new RemoteServiceException(UNREACHABLE + ": " + describe(e), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteServiceException("Interrupted while calling the calculation service", e);
        }
    }

    private ExistingCalculationDto parse(String body) {
        ExistingCalculationDto dto;
        try {
            dto = mapper.readValue(body, ExistingCalculationDto.class);
        } catch (JsonProcessingException e) {
            throw new RemoteServiceException(INVALID_PAYLOAD + e.getOriginalMessage(), e);
        }
        if (dto == null) {
            throw new RemoteServiceException(INVALID_PAYLOAD + "empty body");
        }
        List<String> missing = dto.missingProperties();
        if (!missing.isEmpty()) {
            throw new RemoteServiceException(INVALID_PAYLOAD + "missing "
                    + missing.stream().map(name -> "'" + name + "'").collect(Collectors.joining(", ")));
        }
        return dto;
    }

    /** Uses the same parsers as the console commands, so loaded data follows exactly the same rules. */
    private static LoanDraft toDraft(ExistingCalculationDto dto) {
        try {
            return new LoanDraft()
                    .vehicleType(VehicleType.parse(dto.vehicleType()))
                    .condition(VehicleCondition.parse(dto.vehicleCondition()))
                    .vehicleYear(InputParser.parseYear(String.valueOf(dto.vehicleYear())))
                    .loanAmount(InputParser.parseAmount(plain(dto.totalLoanAmount()), "Loan amount"))
                    .tenor(dto.loanTenure())
                    .downPayment(InputParser.parseAmount(plain(dto.downPayment()), "DP"));
        } catch (InvalidInputException e) {
            throw new RemoteServiceException(INVALID_PAYLOAD + e.getMessage(), e);
        }
    }

    private static String plain(BigDecimal amount) {
        return amount.signum() == 0 ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private static String describe(IOException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private static String seconds(Duration duration) {
        return duration.toMillis() % 1000 == 0 ? duration.toSeconds() + "s" : duration.toMillis() + "ms";
    }
}
