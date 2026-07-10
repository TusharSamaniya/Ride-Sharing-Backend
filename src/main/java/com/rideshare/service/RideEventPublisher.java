package com.rideshare.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RideEventPublisher {

    // In production (Render), Kafka is not available
    // Events are logged instead
    // In local Docker, full Kafka works

    public void publishRideRequested(Long rideId) {
        log.info("EVENT: ride.requested → rideId:{}", rideId);
    }

    public void publishRideAccepted(Long rideId) {
        log.info("EVENT: ride.accepted → rideId:{}", rideId);
    }

    public void publishRideCompleted(Long rideId) {
        log.info("EVENT: ride.completed → rideId:{}", rideId);
    }

    public void publishRideCancelled(Long rideId) {
        log.info("EVENT: ride.cancelled → rideId:{}", rideId);
    }
}