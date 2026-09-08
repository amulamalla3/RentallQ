package com.rentaliq.backend.property;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    boolean existsByAddressIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCase(String address, String city, String state);
}
