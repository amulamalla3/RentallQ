package com.rentaliq.backend.property;

import com.rentaliq.backend.common.DuplicateResourceException;
import com.rentaliq.backend.common.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PropertyService {
    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        String state = request.state().trim().toUpperCase();
        if (propertyRepository.existsByAddressIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCase(
                request.address().trim(), request.city().trim(), state)) {
            throw new DuplicateResourceException("A property with this address already exists");
        }

        Property property = new Property();
        property.setName(request.name().trim());
        property.setAddress(request.address().trim());
        property.setCity(request.city().trim());
        property.setState(state);
        property.setBedrooms(request.bedrooms());
        property.setBathrooms(request.bathrooms());
        property.setSquareFeet(request.squareFeet());
        property.setCurrentMonthlyRent(request.currentMonthlyRent().setScale(2, RoundingMode.HALF_UP));
        property.setOccupancyRate(request.occupancyRate());
        property.setMonthlyExpenses(request.monthlyExpenses().setScale(2, RoundingMode.HALF_UP));

        try {
            return PropertyResponse.from(propertyRepository.save(property));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("A property with this address already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> findAll() {
        return propertyRepository.findAll().stream().map(PropertyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Property getEntity(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property " + id + " was not found"));
    }

    @Transactional(readOnly = true)
    public PropertyResponse findById(Long id) {
        return PropertyResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary() {
        List<Property> properties = propertyRepository.findAll();
        if (properties.isEmpty()) {
            return new PortfolioSummaryResponse(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
        }

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        double occupancy = 0.0;

        for (Property property : properties) {
            revenue = revenue.add(property.getCurrentMonthlyRent()
                    .multiply(BigDecimal.valueOf(property.getOccupancyRate())));
            expenses = expenses.add(property.getMonthlyExpenses());
            occupancy += property.getOccupancyRate();
        }

        return new PortfolioSummaryResponse(
                properties.size(),
                revenue.setScale(2, RoundingMode.HALF_UP),
                expenses.setScale(2, RoundingMode.HALF_UP),
                revenue.subtract(expenses).setScale(2, RoundingMode.HALF_UP),
                occupancy / properties.size()
        );
    }
}
