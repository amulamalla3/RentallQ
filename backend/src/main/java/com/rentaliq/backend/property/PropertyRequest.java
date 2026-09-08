package com.rentaliq.backend.property;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PropertyRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 180) String address,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter abbreviation") String state,
        @NotNull @Min(0) @Max(20) Integer bedrooms,
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") Double bathrooms,
        @NotNull @Min(100) @Max(50000) Integer squareFeet,
        @NotNull @DecimalMin("0.0") BigDecimal currentMonthlyRent,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double occupancyRate,
        @NotNull @DecimalMin("0.0") BigDecimal monthlyExpenses
) {}
