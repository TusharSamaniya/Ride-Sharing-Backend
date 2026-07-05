package com.rideshare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rideshare.dto.driver.DriverProfileDto;
import com.rideshare.dto.driver.RegisterDriverRequest;
import com.rideshare.entity.Driver;
import com.rideshare.entity.User;
import com.rideshare.entity.Vehicle;
import com.rideshare.util.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverService {

	@Autowired
    private DriverRepository driverRepository;
	@Autowired
    private VehicleRepository vehicleRepository;
	@Autowired
    private AuthUtil authUtil;

    // POST /api/drivers/register
    public DriverProfileDto registerDriver(RegisterDriverRequest request) {
        User currentUser = authUtil.getCurrentUser();

        // Check if already registered as driver
        if (driverRepository.existsByUserId(currentUser.getId())) {
            throw new RuntimeException("You are already registered as a driver");
        }

        // Step 1 — create and save the Driver record
        Driver driver = new Driver();
        driver.setUser(currentUser);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setIsAvailable(false);   // starts as offline
        driver.setIsVerified(false);    // admin must verify first
        driver.setAverageRating(0.0);
        driver.setTotalRides(0);
        Driver savedDriver = driverRepository.save(driver);

        // Step 2 — create and save the Vehicle record linked to this driver
        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(savedDriver);
        vehicle.setMake(request.getVehicleMake());
        vehicle.setModel(request.getVehicleModel());
        vehicle.setColor(request.getVehicleColor());
        vehicle.setYear(request.getVehicleYear());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVehicleType(request.getVehicleType());
        vehicleRepository.save(vehicle);

        // Step 3 — return the full driver profile as DTO
        return convertToDto(savedDriver, vehicle);
    }

    // GET /api/drivers/me
    public DriverProfileDto getMyDriverProfile() {
        User currentUser = authUtil.getCurrentUser();

        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));

        Vehicle vehicle = vehicleRepository.findByDriverId(driver.getId())
                .orElse(null);

        return convertToDto(driver, vehicle);
    }

    // PUT /api/drivers/availability?available=true or false
    public String updateAvailability(boolean isAvailable) {
        User currentUser = authUtil.getCurrentUser();

        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));

        // Cannot go online if admin has not verified yet
        if (isAvailable && !driver.getIsVerified()) {
            throw new RuntimeException("Your account is not verified yet. Please wait for admin approval.");
        }

        driver.setIsAvailable(isAvailable);
        driverRepository.save(driver);

        return isAvailable ? "You are now online" : "You are now offline";
    }

    // Private helper — converts Driver + Vehicle into one DriverProfileDto
    private DriverProfileDto convertToDto(Driver driver, Vehicle vehicle) {
        DriverProfileDto dto = new DriverProfileDto();
        dto.setDriverId(driver.getId());
        dto.setFirstName(driver.getUser().getFirstName());
        dto.setLastName(driver.getUser().getLastName());
        dto.setEmail(driver.getUser().getEmail());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setIsAvailable(driver.getIsAvailable());
        dto.setIsVerified(driver.getIsVerified());
        dto.setAverageRating(driver.getAverageRating());
        dto.setTotalRides(driver.getTotalRides());

        if (vehicle != null) {
            dto.setVehicleMake(vehicle.getMake());
            dto.setVehicleModel(vehicle.getModel());
            dto.setVehicleColor(vehicle.getColor());
            dto.setLicensePlate(vehicle.getLicensePlate());
            dto.setVehicleType(vehicle.getVehicleType().name());
        }

        return dto;
    }
}