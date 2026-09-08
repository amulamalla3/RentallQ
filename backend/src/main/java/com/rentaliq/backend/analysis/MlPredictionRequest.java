package com.rentaliq.backend.analysis;

public record MlPredictionRequest(
        String city,
        int bedrooms,
        double bathrooms,
        int squareFeet,
        double occupancyRate,
        double currentRent
) {}
