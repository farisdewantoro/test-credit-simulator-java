package com.creditsimulator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON shape returned by the calculation web service:
 * <pre>{"vehicleType":"Mobil","vehicleCondition":"Baru","vehicleYear":2025,
 *  "totalLoanAmount":1000000000,"loanTenure":6,"downPayment":500000000}</pre>
 * Unknown properties are ignored so new server fields don't break the client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExistingCalculationDto(
        String vehicleType,
        String vehicleCondition,
        Integer vehicleYear,
        BigDecimal totalLoanAmount,
        Integer loanTenure,
        BigDecimal downPayment) {

    /** Names of the properties that are absent or null, in declaration order. */
    public List<String> missingProperties() {
        List<String> missing = new ArrayList<>();
        if (vehicleType == null) {
            missing.add("vehicleType");
        }
        if (vehicleCondition == null) {
            missing.add("vehicleCondition");
        }
        if (vehicleYear == null) {
            missing.add("vehicleYear");
        }
        if (totalLoanAmount == null) {
            missing.add("totalLoanAmount");
        }
        if (loanTenure == null) {
            missing.add("loanTenure");
        }
        if (downPayment == null) {
            missing.add("downPayment");
        }
        return missing;
    }
}
