package com.rideshare.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileDto {
	
	private Long driverId;
    private String firstName;
    private String lastName;
    private String email;
    private String licenseNumber;
    private Boolean isAvailable;
    private Boolean isVerified;
    private Double averageRating;
    private Integer totalRides;

    
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleColor;
    private String licensePlate;
    private String vehicleType;

}
