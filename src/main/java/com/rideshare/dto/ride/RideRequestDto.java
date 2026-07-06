package com.rideshare.dto.ride;

import jakarta.validation.constraints.NotNull;
import lombok.*;

// Rider sends this JSON body when booking a ride
// They send the GPS coordinates of pickup and dropoff
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDto {

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    // Human readable address - optional
    private String pickupAddress;

    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLongitude;

    // Human readable address - optional
    private String dropoffAddress;
}