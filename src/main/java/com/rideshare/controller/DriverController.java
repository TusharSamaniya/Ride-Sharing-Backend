package com.rideshare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideshare.dto.driver.DriverProfileDto;
import com.rideshare.dto.driver.RegisterDriverRequest;
import com.rideshare.entity.Driver;
import com.rideshare.entity.User;
import com.rideshare.repository.DriverRepository;
import com.rideshare.service.DriverService;
import com.rideshare.service.LocationService;
import com.rideshare.util.AuthUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
	
	
	private final DriverService driverService;
	
	
	private final LocationService locationService;
	
	
	private final AuthUtil authUtil;
	
	
	private final DriverRepository driverRepository;
	
	@PostMapping("/register")
    public ResponseEntity<DriverProfileDto> registerAsDriver(
            @Valid @RequestBody RegisterDriverRequest request) {
        return ResponseEntity.ok(driverService.registerDriver(request));
    }

    // GET /api/drivers/me
    @GetMapping("/me")
    public ResponseEntity<DriverProfileDto> getMyDriverProfile() {
        return ResponseEntity.ok(driverService.getMyDriverProfile());
    }

    // PUT /api/drivers/availability?available=true
    @PutMapping("/availability")
    public ResponseEntity<String> updateAvailability(
            @RequestParam boolean available) {
        return ResponseEntity.ok(driverService.updateAvailability(available));
    }
    
    @PutMapping("/location")
    public ResponseEntity<String> updateLocation(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        User currentUser = authUtil.getCurrentUser();
        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));

        // Save location to PostgreSQL (works on all environments)
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driverRepository.save(driver);

        // Also try Redis (only works locally with Docker)
        try {
            locationService.updateDriverLocation(driver.getId(), latitude, longitude);
        } catch (Exception e) {
            // Redis not available in production — that is fine
            log.warn("Redis not available, location saved to DB only");
        }

        return ResponseEntity.ok("Location updated");
    }
}
