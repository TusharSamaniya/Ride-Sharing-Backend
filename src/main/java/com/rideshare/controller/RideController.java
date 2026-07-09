package com.rideshare.controller;

import com.rideshare.dto.ride.RideRequestDto;
import com.rideshare.dto.ride.RideResponseDto;
import com.rideshare.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/request")
    public ResponseEntity<RideResponseDto> requestRide(
            @Valid @RequestBody RideRequestDto request) {
        return ResponseEntity.ok(rideService.requestRide(request));
    }

    @GetMapping("/my-rides")
    public ResponseEntity<List<RideResponseDto>> getMyRides() {
        return ResponseEntity.ok(rideService.getMyRides());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponseDto> getRideById(
            @PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<RideResponseDto> acceptRide(
            @PathVariable Long id) {
        return ResponseEntity.ok(rideService.acceptRide(id));
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<RideResponseDto> startRide(
            @PathVariable Long id) {
        return ResponseEntity.ok(rideService.startRide(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<RideResponseDto> completeRide(
            @PathVariable Long id) {
        return ResponseEntity.ok(rideService.completeRide(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<RideResponseDto> cancelRide(
            @PathVariable Long id,
            @RequestParam(required = false,
                    defaultValue = "Cancelled by user") String reason) {
        return ResponseEntity.ok(rideService.cancelRide(id, reason));
    }
    
 // Pending rides waiting for a driver to accept (driver dashboard)
    @GetMapping("/available")
    public ResponseEntity<List<RideResponseDto>> getAvailableRides() {
        return ResponseEntity.ok(rideService.getAvailableRides());
    }

    // Rides assigned to the currently logged-in driver
    @GetMapping("/driver")
    public ResponseEntity<List<RideResponseDto>> getDriverRides() {
        return ResponseEntity.ok(rideService.getDriverRides());
    }
}