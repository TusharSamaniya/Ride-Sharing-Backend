package com.rideshare.kafka;

import com.rideshare.entity.Ride;
import com.rideshare.repository.RideRepository;
import com.rideshare.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "docker"})   // ← ADD THIS LINE — only runs locally
public class RideEventConsumer {

    private final RideRepository rideRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "ride.requested", groupId = "rideshare-group")
    public void onRideRequested(String message) {
        log.info("Kafka received on ride.requested: {}", message);
        try {
            Long rideId = extractRideId(message);
            Ride ride = rideRepository.findById(rideId).orElse(null);
            if (ride == null || ride.getDriver() == null) return;
            emailService.sendRideRequestedEmailToDriver(
                    ride.getDriver().getUser().getEmail(),
                    ride.getDriver().getUser().getFirstName(),
                    ride.getRider().getFirstName() + " " + ride.getRider().getLastName(),
                    rideId,
                    ride.getPickupAddress() != null ? ride.getPickupAddress() : "See app"
            );
        } catch (Exception e) {
            log.error("Error processing ride.requested: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "ride.accepted", groupId = "rideshare-group")
    public void onRideAccepted(String message) {
        log.info("Kafka received on ride.accepted: {}", message);
        try {
            Long rideId = extractRideId(message);
            Ride ride = rideRepository.findById(rideId).orElse(null);
            if (ride == null || ride.getDriver() == null) return;
            emailService.sendRideAcceptedEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    ride.getDriver().getUser().getFirstName() + " " + ride.getDriver().getUser().getLastName(),
                    ride.getDriver().getUser().getPhoneNumber(),
                    rideId
            );
        } catch (Exception e) {
            log.error("Error processing ride.accepted: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "ride.completed", groupId = "rideshare-group")
    public void onRideCompleted(String message) {
        log.info("Kafka received on ride.completed: {}", message);
        try {
            Long rideId = extractRideId(message);
            Ride ride = rideRepository.findById(rideId).orElse(null);
            if (ride == null) return;
            String fare = ride.getFinalFare() != null ? ride.getFinalFare().toString() : "0";
            emailService.sendRideCompletedEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    rideId,
                    ride.getDropoffAddress() != null ? ride.getDropoffAddress() : "Destination",
                    fare
            );
        } catch (Exception e) {
            log.error("Error processing ride.completed: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "ride.cancelled", groupId = "rideshare-group")
    public void onRideCancelled(String message) {
        log.info("Kafka received on ride.cancelled: {}", message);
        try {
            Long rideId = extractRideId(message);
            Ride ride = rideRepository.findById(rideId).orElse(null);
            if (ride == null) return;
            emailService.sendRideCancelledEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    rideId,
                    ride.getCancellationReason() != null ? ride.getCancellationReason() : "Cancelled"
            );
        } catch (Exception e) {
            log.error("Error processing ride.cancelled: {}", e.getMessage());
        }
    }

    private Long extractRideId(String message) {
        return Long.parseLong(message.split(":")[1].trim());
    }
}