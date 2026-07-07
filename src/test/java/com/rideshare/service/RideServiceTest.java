package com.rideshare.service;

import com.rideshare.dto.ride.RideRequestDto;
import com.rideshare.dto.ride.RideResponseDto;
import com.rideshare.entity.*;
import com.rideshare.repository.*;
import com.rideshare.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuthUtil authUtil;
    @Mock private LocationService locationService;
    @Mock private RideEventPublisher eventPublisher;
    @Mock private WebSocketNotificationService wsNotifier;

    @InjectMocks
    private RideService rideService;

    private User rider;
    private User driverUser;
    private Driver driver;
    private Ride ride;

    @BeforeEach
    void setUp() {
        // Set up rider
        rider = new User();
        rider.setId(1L);
        rider.setFirstName("Rahul");
        rider.setLastName("Sharma");
        rider.setEmail("rahul@test.com");
        rider.setRole(UserRole.RIDER);

        // Set up driver user
        driverUser = new User();
        driverUser.setId(2L);
        driverUser.setFirstName("Ravi");
        driverUser.setLastName("Singh");
        driverUser.setEmail("ravi@test.com");
        driverUser.setPhoneNumber("9876543210");
        driverUser.setRole(UserRole.DRIVER);

        // Set up driver
        driver = new Driver();
        driver.setId(1L);
        driver.setUser(driverUser);
        driver.setIsAvailable(true);
        driver.setIsVerified(true);
        driver.setAverageRating(4.5);
        driver.setTotalRides(10);
        driver.setCurrentLatitude(28.6139);
        driver.setCurrentLongitude(77.2090);

        // Set up a basic ride
        ride = new Ride();
        ride.setId(1L);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.REQUESTED);
        ride.setPickupLatitude(BigDecimal.valueOf(28.6200));
        ride.setPickupLongitude(BigDecimal.valueOf(77.2100));
        ride.setPickupAddress("Connaught Place");
        ride.setDropoffLatitude(BigDecimal.valueOf(28.5355));
        ride.setDropoffLongitude(BigDecimal.valueOf(77.3910));
        ride.setDropoffAddress("Noida Sector 18");
        ride.setEstimatedFare(BigDecimal.valueOf(145.00));
    }

    // ── TEST 1 ────────────────────────────────────────────
    // Test: request ride successfully
    // ─────────────────────────────────────────────────────
    @Test
    void requestRide_withAvailableDriver_shouldReturnRide() {

        // ARRANGE
        RideRequestDto request = new RideRequestDto();
        request.setPickupLatitude(28.6200);
        request.setPickupLongitude(77.2100);
        request.setPickupAddress("Connaught Place");
        request.setDropoffLatitude(28.5355);
        request.setDropoffLongitude(77.3910);
        request.setDropoffAddress("Noida Sector 18");

        when(authUtil.getCurrentUser()).thenReturn(rider);
        when(rideRepository.findByStatus(RideStatus.REQUESTED))
                .thenReturn(List.of());
        when(locationService.findNearbyDriverIds(
                anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(1L));
        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        // ACT
        RideResponseDto result = rideService.requestRide(request);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getStatus())
                .isEqualTo("REQUESTED");
        assertThat(result.getRiderName())
                .contains("Rahul");

        // Kafka event should be published
        verify(eventPublisher, times(1))
                .publishRideRequested(1L);

        // WebSocket notification should be sent
        verify(wsNotifier, times(1))
                .sendRideStatusUpdate(1L, "REQUESTED");
    }

    // ── TEST 2 ────────────────────────────────────────────
    // Test: request ride when no drivers nearby → error
    // ─────────────────────────────────────────────────────
    @Test
    void requestRide_withNoDriversNearby_shouldThrowException() {

        // ARRANGE
        RideRequestDto request = new RideRequestDto();
        request.setPickupLatitude(28.6200);
        request.setPickupLongitude(77.2100);
        request.setDropoffLatitude(28.5355);
        request.setDropoffLongitude(77.3910);

        when(authUtil.getCurrentUser()).thenReturn(rider);
        when(rideRepository.findByStatus(RideStatus.REQUESTED))
                .thenReturn(List.of());

        // No drivers nearby
        when(locationService.findNearbyDriverIds(
                anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        // ACT + ASSERT
        assertThatThrownBy(() ->
                rideService.requestRide(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No drivers available");
    }

    // ── TEST 3 ────────────────────────────────────────────
    // Test: accept ride successfully
    // ─────────────────────────────────────────────────────
    @Test
    void acceptRide_withRequestedRide_shouldChangeStatusToAccepted() {

        // ARRANGE
        when(authUtil.getCurrentUser()).thenReturn(driverUser);
        when(driverRepository.findByUserId(2L))
                .thenReturn(Optional.of(driver));
        when(rideRepository.findById(1L))
                .thenReturn(Optional.of(ride));

        // When save is called, update the ride status
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        // ACT
        RideResponseDto result = rideService.acceptRide(1L);

        // ASSERT
        assertThat(result.getStatus())
                .isEqualTo("ACCEPTED");
        verify(eventPublisher, times(1))
                .publishRideAccepted(1L);
    }

    // ── TEST 4 ────────────────────────────────────────────
    // Test: complete a ride
    // ─────────────────────────────────────────────────────
    @Test
    void completeRide_withInProgressRide_shouldComplete() {

        // ARRANGE — ride is IN_PROGRESS
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(java.time.LocalDateTime.now()
                .minusMinutes(20));

        when(rideRepository.findById(1L))
                .thenReturn(Optional.of(ride));

        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        // ACT
        RideResponseDto result = rideService.completeRide(1L);

        // ASSERT
        assertThat(result.getStatus())
                .isEqualTo("COMPLETED");
        verify(eventPublisher, times(1))
                .publishRideCompleted(1L);
    }

    // ── TEST 5 ────────────────────────────────────────────
    // Test: get ride by id
    // ─────────────────────────────────────────────────────
    @Test
    void getRideById_withValidId_shouldReturnRide() {

        // ARRANGE
        when(rideRepository.findById(1L))
                .thenReturn(Optional.of(ride));

        // ACT
        RideResponseDto result = rideService.getRideById(1L);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getRideId()).isEqualTo(1L);
    }

    // ── TEST 6 ────────────────────────────────────────────
    // Test: get ride with wrong id → error
    // ─────────────────────────────────────────────────────
    @Test
    void getRideById_withInvalidId_shouldThrowException() {

        // ARRANGE — ride does not exist
        when(rideRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() ->
                rideService.getRideById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride not found");
    }
}