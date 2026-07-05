package com.rideshare.dto.driver;

import com.rideshare.entity.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

// Driver sends this when registering: POST /api/drivers/register
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDriverRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Vehicle make is required")
    private String vehicleMake;       // example: Toyota

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;      // example: Innova

    private String vehicleColor;

    private Integer vehicleYear;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;  // 
}