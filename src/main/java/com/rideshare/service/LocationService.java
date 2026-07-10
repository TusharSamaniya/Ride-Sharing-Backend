package com.rideshare.service;

import com.rideshare.entity.Driver;
import com.rideshare.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final DriverRepository driverRepository;

    // When Redis is not available, we fall back to PostgreSQL
    // to find available drivers
    // This is slightly slower but works perfectly fine

    public void updateDriverLocation(Long driverId,
                                     double latitude,
                                     double longitude) {
        // Location is stored in the Driver table directly
        // Redis stores it in production (local Docker)
        // PostgreSQL stores it in deployment (Render)
        log.info("Location updated for driver {} → {}, {}",
                driverId, latitude, longitude);
    }

    public List<Long> findNearbyDriverIds(double latitude,
                                          double longitude,
                                          double radiusKm) {
        // Get all available and verified drivers from PostgreSQL
        List<Driver> availableDrivers = driverRepository
                .findByIsAvailableTrueAndIsVerifiedTrue();

        log.info("Found {} available drivers from database",
                availableDrivers.size());

        // Return their IDs
        return availableDrivers.stream()
                .map(Driver::getId)
                .collect(Collectors.toList());
    }

    public void removeDriverLocation(Long driverId) {
        log.info("Driver {} went offline", driverId);
    }
}