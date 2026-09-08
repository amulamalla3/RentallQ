package com.rentaliq.backend.property;

import com.rentaliq.backend.analysis.AnalysisResponse;
import com.rentaliq.backend.analysis.PropertyAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PropertyController {
    private final PropertyService propertyService;
    private final PropertyAnalysisService propertyAnalysisService;

    public PropertyController(PropertyService propertyService, PropertyAnalysisService propertyAnalysisService) {
        this.propertyService = propertyService;
        this.propertyAnalysisService = propertyAnalysisService;
    }

    @PostMapping("/properties")
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.create(request));
    }

    @GetMapping("/properties")
    public List<PropertyResponse> findAll() {
        return propertyService.findAll();
    }

    @GetMapping("/properties/{id}")
    public PropertyResponse findById(@PathVariable Long id) {
        return propertyService.findById(id);
    }

    @GetMapping("/portfolio/summary")
    public PortfolioSummaryResponse portfolioSummary() {
        return propertyService.getPortfolioSummary();
    }

    @PostMapping("/properties/{id}/analyze")
    public AnalysisResponse analyze(@PathVariable Long id) {
        return propertyAnalysisService.analyze(id);
    }
}
