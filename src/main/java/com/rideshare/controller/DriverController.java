package com.rideshare.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private DriverService driverService;
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private AuthUtil authUtil;
	
	@Autowired
	private DriverRepository driverRepository;
	
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

        // Get the current logged-in driver
        User currentUser = authUtil.getCurrentUser();
        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Save to Redis (fast, real-time)
        locationService.updateDriverLocation(
                driver.getId(), latitude, longitude);

        // Also save to PostgreSQL (permanent record)
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driverRepository.save(driver);
        
        return ResponseEntity.ok("Location.updated");
    }
}
