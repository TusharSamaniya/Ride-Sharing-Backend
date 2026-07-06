package com.rideshare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideEventPublisher {
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	public void publishRideRequested(Long rideId) {
		String message = "rideId:" + rideId;
		kafkaTemplate.send("ride.requested", message);
		log.info("published to ride.requested -> {}", message);
	}
	
	public void publishRideAccepted(Long rideId) {
        String message = "rideId:" + rideId;
        kafkaTemplate.send("ride.accepted", message);
        log.info("Published to ride.accepted → {}", message);
    }
	
	public void publishRideCompleted(Long rideId) {
        String message = "rideId:" + rideId;
        kafkaTemplate.send("ride.completed", message);
        log.info("Published to ride.completed → {}", message);
    }
	
	public void publishRideCancelled(Long rideId) {
        String message = "rideId:" + rideId;
        kafkaTemplate.send("ride.cancelled", message);
        log.info("Published to ride.cancelled → {}", message);
    }

}
