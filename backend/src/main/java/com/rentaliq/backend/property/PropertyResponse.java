package com.rentaliq.backend.property;

import java.math.BigDecimal;
import java.time.Instant;

public record PropertyResponse(
        Long id,
        String name,
        String address,
        String city,
        String state,
        Integer bedrooms,
        Double bathrooms,
        Integer squareFeet,
        BigDecimal currentMonthlyRent,
        Double occupancyRate,
        BigDecimal monthlyExpenses,
        BigDecimal estimatedMonthlyRevenue,
        BigDecimal estimatedMonthlyProfit,
        Instant createdAt
) {
    public static PropertyResponse from(Property property) {
        BigDecimal revenue = property.getCurrentMonthlyRent()
                .multiply(BigDecimal.valueOf(property.getOccupancyRate()))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal profit = revenue.subtract(property.getMonthlyExpenses())
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getCity(),
                property.getState(),
                property.getBedrooms(),
                property.getBathrooms(),
                property.getSquareFeet(),
                property.getCurrentMonthlyRent(),
                property.getOccupancyRate(),
                property.getMonthlyExpenses(),
                revenue,
                profit,
                property.getCreatedAt()
        );
    }
}
