package com.rideshare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // Push ride status update to anyone listening on this topic
    // The rider's frontend subscribes to /topic/ride/{rideId}/status
    // When we call this method, rider sees status update instantly
    public void sendRideStatusUpdate(Long rideId, String status) {
        String destination = "/topic/ride/" + rideId + "/status";
        messagingTemplate.convertAndSend(destination, status);
        log.info("WebSocket sent to {} → status: {}", destination, status);
    }

    // Push driver location update to rider
    // Rider's map shows driver moving in real-time
    public void sendDriverLocation(Long rideId,
                                    double latitude,
                                    double longitude) {
        String destination = "/topic/ride/" + rideId + "/driver-location";
        String locationJson = "{\"latitude\":" + latitude
                + ",\"longitude\":" + longitude + "}";
        messagingTemplate.convertAndSend(destination, locationJson);
    }
}