package com.rentaliq.backend.demo;

import com.rentaliq.backend.property.Property;
import com.rentaliq.backend.property.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private final PropertyRepository repository;
    private final boolean enabled;

    public DemoDataSeeder(PropertyRepository repository,
                          @Value("${app.demo.seed}") boolean enabled) {
        this.repository = repository;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || repository.count() > 0) {
            return;
        }

        repository.save(property(
                "Midtown Atlanta Condo", "1100 Peachtree Demo St", "Atlanta", "GA",
                2, 2.0, 1180, 1850, 0.97, 520));
        repository.save(property(
                "Buckhead Townhome", "3200 Lenox Demo Rd", "Atlanta", "GA",
                3, 2.5, 1760, 2850, 0.91, 890));
        repository.save(property(
                "Old Fourth Ward Loft", "700 Edgewood Demo Ave", "Atlanta", "GA",
                1, 1.0, 820, 1725, 0.84, 455));
    }

    private Property property(String name, String address, String city, String state,
                              int bedrooms, double bathrooms, int squareFeet,
                              double rent, double occupancy, double expenses) {
        Property property = new Property();
        property.setName(name);
        property.setAddress(address);
        property.setCity(city);
        property.setState(state);
        property.setBedrooms(bedrooms);
        property.setBathrooms(bathrooms);
        property.setSquareFeet(squareFeet);
        property.setCurrentMonthlyRent(BigDecimal.valueOf(rent));
        property.setOccupancyRate(occupancy);
        property.setMonthlyExpenses(BigDecimal.valueOf(expenses));
        return property;
    }
}
