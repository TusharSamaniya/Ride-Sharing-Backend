package com.rideshare.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.rideshare.entity.Ride;
import com.rideshare.repository.RideRepository;
import com.rideshare.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventConsumer {

	
    private final RideRepository rideRepository;
	
    private final EmailService emailService;

    // ──────────────────────────────────────────────────────
    // HOW THIS WORKS:
    // RideEventPublisher sends "rideId:1" to Kafka topic
    // This method receives that message automatically
    // We extract the rideId, fetch the ride from DB
    // Then send the correct email
    // ──────────────────────────────────────────────────────

    // Listens to "ride.requested" topic
    // Sends email to DRIVER about new ride
    @KafkaListener(
            topics = "ride.requested",
            groupId = "rideshare-group"
    )
    public void onRideRequested(String message) {
        log.info("Kafka received on ride.requested: {}", message);

        try {
            // Message format is "rideId:1"
            // Extract the number after the colon
            Long rideId = extractRideId(message);

            Ride ride = rideRepository.findById(rideId)
                    .orElse(null);

            if (ride == null || ride.getDriver() == null) {
                log.warn("Ride {} not found or no driver assigned",
                        rideId);
                return;
            }

            // Send email to the DRIVER
            emailService.sendRideRequestedEmailToDriver(
                    ride.getDriver().getUser().getEmail(),
                    ride.getDriver().getUser().getFirstName(),
                    ride.getRider().getFirstName()
                            + " " + ride.getRider().getLastName(),
                    rideId,
                    ride.getPickupAddress() != null
                            ? ride.getPickupAddress()
                            : "See app for location"
            );

        } catch (Exception e) {
            log.error("Error processing ride.requested event: {}",
                    e.getMessage());
        }
    }

    // Listens to "ride.accepted" topic
    // Sends email to RIDER that driver is coming
    @KafkaListener(
            topics = "ride.accepted",
            groupId = "rideshare-group"
    )
    public void onRideAccepted(String message) {
        log.info("Kafka received on ride.accepted: {}", message);

        try {
            Long rideId = extractRideId(message);

            Ride ride = rideRepository.findById(rideId)
                    .orElse(null);

            if (ride == null || ride.getDriver() == null) {
                log.warn("Ride {} not found or no driver", rideId);
                return;
            }

            // Send email to the RIDER
            emailService.sendRideAcceptedEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    ride.getDriver().getUser().getFirstName()
                            + " " + ride.getDriver().getUser()
                            .getLastName(),
                    ride.getDriver().getUser().getPhoneNumber(),
                    rideId
            );

        } catch (Exception e) {
            log.error("Error processing ride.accepted event: {}",
                    e.getMessage());
        }
    }

    // Listens to "ride.completed" topic
    // Sends email to both RIDER and DRIVER
    @KafkaListener(
            topics = "ride.completed",
            groupId = "rideshare-group"
    )
    public void onRideCompleted(String message) {
        log.info("Kafka received on ride.completed: {}", message);

        try {
            Long rideId = extractRideId(message);

            Ride ride = rideRepository.findById(rideId)
                    .orElse(null);

            if (ride == null) {
                log.warn("Ride {} not found", rideId);
                return;
            }

            String fare = ride.getFinalFare() != null
                    ? ride.getFinalFare().toString()
                    : "0.00";

            String dropoff = ride.getDropoffAddress() != null
                    ? ride.getDropoffAddress()
                    : "Destination";

            // Email to RIDER
            emailService.sendRideCompletedEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    rideId,
                    dropoff,
                    fare
            );

            // Email to DRIVER
            if (ride.getDriver() != null) {
                emailService.sendRideCompletedEmailToDriver(
                        ride.getDriver().getUser().getEmail(),
                        ride.getDriver().getUser().getFirstName(),
                        rideId,
                        fare
                );
            }

        } catch (Exception e) {
            log.error("Error processing ride.completed event: {}",
                    e.getMessage());
        }
    }

    // Listens to "ride.cancelled" topic
    // Sends email to both RIDER and DRIVER
    @KafkaListener(
            topics = "ride.cancelled",
            groupId = "rideshare-group"
    )
    public void onRideCancelled(String message) {
        log.info("Kafka received on ride.cancelled: {}", message);

        try {
            Long rideId = extractRideId(message);

            Ride ride = rideRepository.findById(rideId)
                    .orElse(null);

            if (ride == null) {
                log.warn("Ride {} not found", rideId);
                return;
            }

            String reason = ride.getCancellationReason() != null
                    ? ride.getCancellationReason()
                    : "Cancelled by user";

            // Email to RIDER
            emailService.sendRideCancelledEmailToRider(
                    ride.getRider().getEmail(),
                    ride.getRider().getFirstName(),
                    rideId,
                    reason
            );

            // Email to DRIVER (only if driver was assigned)
            if (ride.getDriver() != null) {
                emailService.sendRideCancelledEmailToDriver(
                        ride.getDriver().getUser().getEmail(),
                        ride.getDriver().getUser().getFirstName(),
                        rideId
                );
            }

        } catch (Exception e) {
            log.error("Error processing ride.cancelled event: {}",
                    e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────
    // PRIVATE HELPER
    // Extracts rideId from message string "rideId:1"
    // ──────────────────────────────────────────────────────
    private Long extractRideId(String message) {
        // message = "rideId:1"
        // split by ":" gives ["rideId", "1"]
        // take index [1] = "1"
        // parse to Long = 1
        String[] parts = message.split(":");
        return Long.parseLong(parts[1].trim());
    }
}