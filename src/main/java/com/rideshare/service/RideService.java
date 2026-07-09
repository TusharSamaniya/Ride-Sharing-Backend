package com.rideshare.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideshare.dto.ride.RideRequestDto;
import com.rideshare.dto.ride.RideResponseDto;
import com.rideshare.entity.Driver;
import com.rideshare.entity.Ride;
import com.rideshare.entity.RideStatus;
import com.rideshare.entity.User;
import com.rideshare.entity.UserRole;
import com.rideshare.repository.DriverRepository;
import com.rideshare.repository.RideRepository;
import com.rideshare.util.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

	
    private final RideRepository rideRepository;
	
    private final DriverRepository driverRepository;
	
	
    private final AuthUtil authUtil;

    // NEW — inject the three new services
	
    private final LocationService locationService;
	
    private final RideEventPublisher eventPublisher;
	
    private final WebSocketNotificationService wsNotifier;

    // ─────────────────────────────────────────────────────
    // POST /api/rides/request
    // ─────────────────────────────────────────────────────
    @Transactional
    public RideResponseDto requestRide(RideRequestDto request) {

        User rider = authUtil.getCurrentUser();

        if (rider.getRole() != UserRole.RIDER) {
            throw new RuntimeException("Only riders can request rides");
        }

        // Step 1: Calculate estimated fare
        BigDecimal estimatedFare = calculateFare(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropoffLatitude(),
                request.getDropoffLongitude()
        );

        // Step 2: Create and save the Ride as REQUESTED with NO driver yet.
        // Drivers will see it in their dashboard and one of them accepts it.
        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setPickupLatitude(BigDecimal.valueOf(request.getPickupLatitude()));
        ride.setPickupLongitude(BigDecimal.valueOf(request.getPickupLongitude()));
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropoffLatitude(BigDecimal.valueOf(request.getDropoffLatitude()));
        ride.setDropoffLongitude(BigDecimal.valueOf(request.getDropoffLongitude()));
        ride.setDropoffAddress(request.getDropoffAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(estimatedFare);

        Ride savedRide = rideRepository.save(ride);

        // Step 3: Best-effort nearby lookup (never block the booking)
        try {
            List<Long> nearbyDriverIds = locationService.findNearbyDriverIds(
                    request.getPickupLatitude(),
                    request.getPickupLongitude(),
                    10.0);
            log.info("Ride {} broadcast to {} nearby driver(s)",
                    savedRide.getId(), nearbyDriverIds.size());
        } catch (Exception e) {
            log.warn("Could not query nearby drivers from Redis: {}", e.getMessage());
        }

        // Step 4: Kafka (best-effort)
        try {
            eventPublisher.publishRideRequested(savedRide.getId());
        } catch (Exception e) {
            log.warn("Kafka publish failed for ride {}: {}", savedRide.getId(), e.getMessage());
        }

        // Step 5: WebSocket (best-effort)
        try {
            wsNotifier.sendRideStatusUpdate(savedRide.getId(), "REQUESTED");
        } catch (Exception e) {
            log.warn("WebSocket notify failed for ride {}: {}", savedRide.getId(), e.getMessage());
        }

        log.info("Ride {} requested by rider {}", savedRide.getId(), rider.getId());
        return convertToDto(savedRide);
    }

    // NEW — pending rides any driver can accept
    public List<RideResponseDto> getAvailableRides() {
        return rideRepository.findByStatus(RideStatus.REQUESTED).stream()
                .filter(ride -> ride.getDriver() == null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // NEW — rides assigned to the logged-in driver
    public List<RideResponseDto> getDriverRides() {
        User currentUser = authUtil.getCurrentUser();
        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
        return rideRepository.findByDriverIdOrderByRequestedAtDesc(driver.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/rides/{id}/accept
    // ─────────────────────────────────────────────────────
    @Transactional
    public RideResponseDto acceptRide(Long rideId) {

        User currentUser = authUtil.getCurrentUser();
        Driver driver = driverRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Driver profile not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Status validation — can only accept REQUESTED rides
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException(
                    "Cannot accept. Ride status is: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setDriver(driver);
        ride.setAcceptedAt(LocalDateTime.now());

        driver.setIsAvailable(false);
        driverRepository.save(driver);

        Ride savedRide = rideRepository.save(ride);

        // Publish to Kafka → NotificationService sends push to rider
        eventPublisher.publishRideAccepted(savedRide.getId());

        // WebSocket → rider sees "Driver is coming" instantly
        wsNotifier.sendRideStatusUpdate(savedRide.getId(), "ACCEPTED");

        return convertToDto(savedRide);
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/rides/{id}/start
    // ─────────────────────────────────────────────────────
    @Transactional
    public RideResponseDto startRide(Long rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Status validation — can only start ACCEPTED rides
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException(
                    "Cannot start. Ride status is: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());

        Ride savedRide = rideRepository.save(ride);

        // WebSocket → rider sees "Ride started" instantly on their screen
        wsNotifier.sendRideStatusUpdate(savedRide.getId(), "IN_PROGRESS");

        return convertToDto(savedRide);
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/rides/{id}/complete
    // ─────────────────────────────────────────────────────
    @Transactional
    public RideResponseDto completeRide(Long rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Status validation — can only complete IN_PROGRESS rides
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RuntimeException(
                    "Cannot complete. Ride status is: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setFinalFare(ride.getEstimatedFare());

        long minutes = java.time.Duration.between(
                ride.getStartedAt(), LocalDateTime.now()).toMinutes();
        ride.setDurationMinutes((int) minutes);

        // Make driver available again
        Driver driver = ride.getDriver();
        driver.setIsAvailable(true);
        driver.setTotalRides(driver.getTotalRides() + 1);
        driverRepository.save(driver);

        Ride savedRide = rideRepository.save(ride);

        // Publish to Kafka → triggers payment processing + notifications
        eventPublisher.publishRideCompleted(savedRide.getId());

        // WebSocket → rider sees "Ride completed" and fare instantly
        wsNotifier.sendRideStatusUpdate(savedRide.getId(), "COMPLETED");

        return convertToDto(savedRide);
    }

    // ─────────────────────────────────────────────────────
    // PUT /api/rides/{id}/cancel
    // ─────────────────────────────────────────────────────
    @Transactional
    public RideResponseDto cancelRide(Long rideId, String reason) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() == RideStatus.IN_PROGRESS
                || ride.getStatus() == RideStatus.COMPLETED
                || ride.getStatus() == RideStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot cancel. Ride status is: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(reason);

        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            driver.setIsAvailable(true);
            driverRepository.save(driver);
        }

        Ride savedRide = rideRepository.save(ride);

        // Publish to Kafka → notify driver that ride was cancelled
        eventPublisher.publishRideCancelled(savedRide.getId());

        // WebSocket → both rider and driver see CANCELLED instantly
        wsNotifier.sendRideStatusUpdate(savedRide.getId(), "CANCELLED");

        return convertToDto(savedRide);
    }

    // ─────────────────────────────────────────────────────
    // GET /api/rides/my-rides
    // ─────────────────────────────────────────────────────
    public List<RideResponseDto> getMyRides() {
        User currentUser = authUtil.getCurrentUser();
        List<Ride> rides = rideRepository
                .findByRiderIdOrderByRequestedAtDesc(currentUser.getId());
        return rides.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    // GET /api/rides/{id}
    // ─────────────────────────────────────────────────────
    public RideResponseDto getRideById(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException(
                        "Ride not found with id: " + rideId));
        return convertToDto(ride);
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE — Fare calculation
    // ─────────────────────────────────────────────────────
    private BigDecimal calculateFare(double pickupLat, double pickupLon,
                                      double dropLat, double dropLon) {
        double distanceKm = calculateDistance(
                pickupLat, pickupLon, dropLat, dropLon);
        double baseFare = 30.0;
        double ratePerKm = 12.0;
        double total = baseFare + (distanceKm * ratePerKm);
        return BigDecimal.valueOf(Math.round(total * 100.0) / 100.0);
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE — Haversine distance formula
    // ─────────────────────────────────────────────────────
    private double calculateDistance(double lat1, double lon1,
                                      double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE — Convert Ride entity to DTO
    // ─────────────────────────────────────────────────────
    private RideResponseDto convertToDto(Ride ride) {
        RideResponseDto dto = new RideResponseDto();
        dto.setRideId(ride.getId());
        dto.setRiderId(ride.getRider().getId());
        dto.setRiderName(ride.getRider().getFirstName()
                + " " + ride.getRider().getLastName());

        if (ride.getDriver() != null) {
            dto.setDriverId(ride.getDriver().getId());
            dto.setDriverName(
                    ride.getDriver().getUser().getFirstName()
                    + " " + ride.getDriver().getUser().getLastName());
            dto.setDriverPhone(
                    ride.getDriver().getUser().getPhoneNumber());
            dto.setDriverRating(ride.getDriver().getAverageRating());
        }

        dto.setPickupLatitude(ride.getPickupLatitude().doubleValue());
        dto.setPickupLongitude(ride.getPickupLongitude().doubleValue());
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setDropoffLatitude(ride.getDropoffLatitude().doubleValue());
        dto.setDropoffLongitude(ride.getDropoffLongitude().doubleValue());
        dto.setDropoffAddress(ride.getDropoffAddress());
        dto.setStatus(ride.getStatus().name());
        dto.setEstimatedFare(ride.getEstimatedFare());
        dto.setFinalFare(ride.getFinalFare());
        dto.setRequestedAt(ride.getRequestedAt());
        dto.setAcceptedAt(ride.getAcceptedAt());
        dto.setStartedAt(ride.getStartedAt());
        dto.setCompletedAt(ride.getCompletedAt());

        return dto;
    }
}
