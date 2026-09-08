package com.rentaliq.backend.property;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "properties",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_property_address_city_state",
                columnNames = {"address", "city", "state"}
        )
)
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Double bathrooms;

    @Column(nullable = false)
    private Integer squareFeet;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentMonthlyRent;

    @Column(nullable = false)
    private Double occupancyRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyExpenses;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    public Double getBathrooms() { return bathrooms; }
    public void setBathrooms(Double bathrooms) { this.bathrooms = bathrooms; }
    public Integer getSquareFeet() { return squareFeet; }
    public void setSquareFeet(Integer squareFeet) { this.squareFeet = squareFeet; }
    public BigDecimal getCurrentMonthlyRent() { return currentMonthlyRent; }
    public void setCurrentMonthlyRent(BigDecimal currentMonthlyRent) { this.currentMonthlyRent = currentMonthlyRent; }
    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
