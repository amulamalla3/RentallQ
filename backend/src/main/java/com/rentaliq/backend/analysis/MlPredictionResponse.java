package com.rentaliq.backend.analysis;

public record MlPredictionResponse(
        double predictedRent,
        double predictedRevenue,
        double confidence,
        String modelVersion,
        double mae,
        double r2
) {}
