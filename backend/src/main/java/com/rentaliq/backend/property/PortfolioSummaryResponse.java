package com.rentaliq.backend.property;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        long propertyCount,
        BigDecimal estimatedMonthlyRevenue,
        BigDecimal estimatedMonthlyExpenses,
        BigDecimal estimatedMonthlyProfit,
        double averageOccupancyRate
) {}
