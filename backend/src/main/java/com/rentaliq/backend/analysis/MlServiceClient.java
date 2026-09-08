package com.rentaliq.backend.analysis;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class MlServiceClient {
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final HttpClient httpClient;

    public MlServiceClient(ObjectMapper objectMapper,
                           @Value("${ml.service.base-url}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {
        return post("/predict", request, MlPredictionResponse.class, Duration.ofSeconds(4));
    }

    public ExplanationResponse explain(ExplanationRequest request) {
        return post("/explain", request, ExplanationResponse.class, Duration.ofSeconds(6));
    }

    private <T> T post(String path, Object payload, Class<T> responseType, Duration timeout) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new DownstreamServiceException("ML service returned HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (JacksonException ex) {
            throw new DownstreamServiceException("Could not serialize or parse ML service response", ex);
        } catch (IOException ex) {
            throw new DownstreamServiceException("ML service network request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DownstreamServiceException("ML service request was interrupted", ex);
        } catch (IllegalArgumentException ex) {
            throw new DownstreamServiceException("ML service URL is invalid", ex);
        }
    }
}
