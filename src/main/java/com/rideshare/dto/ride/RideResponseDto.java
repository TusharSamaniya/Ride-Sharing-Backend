package com.rideshare.dto.ride;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// We return this whenever client asks for ride info
// Contains everything the rider or driver needs to know
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponseDto {

    private Long rideId;

    // Rider info
    private Long riderId;
    private String riderName;

    // Driver info - null until driver accepts
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Double driverRating;

    // Vehicle info - null until driver accepts
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleColor;
    private String licensePlate;

    // Location info
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String dropoffAddress;

    // Ride status
    private String status;

    // Fare info
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private BigDecimal distanceKm;

    // Timestamps
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}