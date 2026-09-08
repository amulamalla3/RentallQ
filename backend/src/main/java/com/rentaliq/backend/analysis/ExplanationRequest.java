package com.rentaliq.backend.analysis;

public record ExplanationRequest(
        String propertyName,
        String city,
        double currentRent,
        double predictedRent,
        double occupancyRate,
        double monthlyExpenses,
        double estimatedCurrentRevenue,
        double predictedRevenue,
        double potentialMonthlyUpside
) {}
