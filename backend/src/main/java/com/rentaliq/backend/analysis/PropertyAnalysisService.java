package com.rentaliq.backend.analysis;

import com.rentaliq.backend.property.Property;
import com.rentaliq.backend.property.PropertyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PropertyAnalysisService {
    private final PropertyService propertyService;
    private final MlServiceClient mlServiceClient;

    public PropertyAnalysisService(PropertyService propertyService, MlServiceClient mlServiceClient) {
        this.propertyService = propertyService;
        this.mlServiceClient = mlServiceClient;
    }

    public AnalysisResponse analyze(Long propertyId) {
        Property property = propertyService.getEntity(propertyId);
        BigDecimal currentRevenue = property.getCurrentMonthlyRent()
                .multiply(BigDecimal.valueOf(property.getOccupancyRate()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentProfit = currentRevenue
                .subtract(property.getMonthlyExpenses())
                .setScale(2, RoundingMode.HALF_UP);

        try {
            MlPredictionResponse prediction = mlServiceClient.predict(new MlPredictionRequest(
                    property.getCity(),
                    property.getBedrooms(),
                    property.getBathrooms(),
                    property.getSquareFeet(),
                    property.getOccupancyRate(),
                    property.getCurrentMonthlyRent().doubleValue()
            ));

            BigDecimal predictedRent = BigDecimal.valueOf(prediction.predictedRent())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal predictedRevenue = BigDecimal.valueOf(prediction.predictedRevenue())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal upside = predictedRevenue.subtract(currentRevenue)
                    .setScale(2, RoundingMode.HALF_UP);

            double rentDifferencePercent = property.getCurrentMonthlyRent().signum() == 0
                    ? 0.0
                    : predictedRent.subtract(property.getCurrentMonthlyRent())
                    .divide(property.getCurrentMonthlyRent(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            ExplanationResponse explanation;
            try {
                explanation = mlServiceClient.explain(new ExplanationRequest(
                        property.getName(),
                        property.getCity(),
                        property.getCurrentMonthlyRent().doubleValue(),
                        predictedRent.doubleValue(),
                        property.getOccupancyRate(),
                        property.getMonthlyExpenses().doubleValue(),
                        currentRevenue.doubleValue(),
                        predictedRevenue.doubleValue(),
                        upside.doubleValue()
                ));
            } catch (DownstreamServiceException ex) {
                explanation = new ExplanationResponse(buildFallbackExplanation(
                        property, predictedRent, rentDifferencePercent, upside), "java-fallback");
            }

            return new AnalysisResponse(
                    property.getId(),
                    property.getName(),
                    property.getCurrentMonthlyRent(),
                    predictedRent,
                    round(rentDifferencePercent),
                    property.getOccupancyRate(),
                    currentRevenue,
                    predictedRevenue,
                    currentProfit,
                    upside,
                    round(prediction.confidence()),
                    explanation.explanation(),
                    explanation.source(),
                    prediction.modelVersion(),
                    round(prediction.mae()),
                    round(prediction.r2()),
                    false
            );
        } catch (DownstreamServiceException ex) {
            return new AnalysisResponse(
                    property.getId(),
                    property.getName(),
                    property.getCurrentMonthlyRent(),
                    property.getCurrentMonthlyRent(),
                    0.0,
                    property.getOccupancyRate(),
                    currentRevenue,
                    currentRevenue,
                    currentProfit,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0.0,
                    "Pricing analysis is temporarily unavailable because the ML service could not be reached. The main property API remains available; try analysis again later.",
                    "degraded-response",
                    "unavailable",
                    0.0,
                    0.0,
                    true
            );
        }
    }

    private String buildFallbackExplanation(Property property,
                                            BigDecimal predictedRent,
                                            double differencePercent,
                                            BigDecimal upside) {
        if (differencePercent > 5) {
            return String.format(
                    "%s appears underpriced relative to the model. Current rent is $%s versus a predicted market rent of $%s. With %.0f%% occupancy, the model estimates about $%s of additional monthly revenue potential, but any price change should be tested against real vacancy and market data.",
                    property.getName(),
                    property.getCurrentMonthlyRent(),
                    predictedRent,
                    property.getOccupancyRate() * 100,
                    upside.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));
        }
        if (differencePercent < -5) {
            return String.format(
                    "%s may be priced above the model estimate. Current rent is $%s versus a predicted market rent of $%s. Review local comparables and occupancy before increasing price further.",
                    property.getName(), property.getCurrentMonthlyRent(), predictedRent);
        }
        return String.format(
                "%s is priced close to the model estimate at $%s. Occupancy is %.0f%%, so the next optimization step is to monitor expenses and local demand rather than make a large rent change.",
                property.getName(), predictedRent, property.getOccupancyRate() * 100);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
