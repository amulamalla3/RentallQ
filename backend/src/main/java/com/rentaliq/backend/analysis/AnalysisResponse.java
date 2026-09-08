package com.rentaliq.backend.analysis;

import java.math.BigDecimal;

public record AnalysisResponse(
        Long propertyId,
        String propertyName,
        BigDecimal currentRent,
        BigDecimal predictedRent,
        double rentDifferencePercent,
        double occupancyRate,
        BigDecimal estimatedCurrentRevenue,
        BigDecimal predictedRevenue,
        BigDecimal estimatedCurrentProfit,
        BigDecimal potentialMonthlyUpside,
        double confidence,
        String recommendation,
        String explanationSource,
        String modelVersion,
        double modelMae,
        double modelR2,
        boolean degradedMode
) {}
