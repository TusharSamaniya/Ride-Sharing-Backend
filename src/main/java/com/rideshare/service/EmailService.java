package com.rideshare.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    // ──────────────────────────────────────────────────────
    // @Async means this method runs in a background thread
    // So sending email does NOT slow down the API response
    // The ride booking completes immediately
    // Email is sent separately in background
    // ──────────────────────────────────────────────────────

    // Email 1 — sent to DRIVER when new ride is assigned
    @Async
    public void sendRideRequestedEmailToDriver(
            String driverEmail,
            String driverName,
            String riderName,
            Long rideId,
            String pickupAddress) {

        String subject = appName + " — New Ride Assigned #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + driverName + ",</h2>"
                + "<p>You have a new ride request!</p>"
                + "<table border='1' cellpadding='8' "
                + "style='border-collapse:collapse;'>"
                + "<tr><td><b>Ride ID</b></td>"
                + "<td>#" + rideId + "</td></tr>"
                + "<tr><td><b>Rider Name</b></td>"
                + "<td>" + riderName + "</td></tr>"
                + "<tr><td><b>Pickup Location</b></td>"
                + "<td>" + pickupAddress + "</td></tr>"
                + "</table>"
                + "<p>Please open the app to accept the ride.</p>"
                + "<br><p>Thanks,<br><b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(driverEmail, subject, body);
    }

    // Email 2 — sent to RIDER when driver accepts the ride
    @Async
    public void sendRideAcceptedEmailToRider(
            String riderEmail,
            String riderName,
            String driverName,
            String driverPhone,
            Long rideId) {

        String subject = appName + " — Driver On The Way! Ride #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + riderName + ",</h2>"
                + "<p>Great news! Your driver is on the way.</p>"
                + "<table border='1' cellpadding='8' "
                + "style='border-collapse:collapse;'>"
                + "<tr><td><b>Ride ID</b></td>"
                + "<td>#" + rideId + "</td></tr>"
                + "<tr><td><b>Driver Name</b></td>"
                + "<td>" + driverName + "</td></tr>"
                + "<tr><td><b>Driver Phone</b></td>"
                + "<td>" + driverPhone + "</td></tr>"
                + "</table>"
                + "<p>Please be ready at your pickup location.</p>"
                + "<br><p>Thanks,<br><b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(riderEmail, subject, body);
    }

    // Email 3 — sent to RIDER when ride completes
    @Async
    public void sendRideCompletedEmailToRider(
            String riderEmail,
            String riderName,
            Long rideId,
            String dropoffAddress,
            String fare) {

        String subject = appName + " — Ride Completed! Ride #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + riderName + ",</h2>"
                + "<p>Your ride has been completed. "
                + "Thank you for riding with us!</p>"
                + "<table border='1' cellpadding='8' "
                + "style='border-collapse:collapse;'>"
                + "<tr><td><b>Ride ID</b></td>"
                + "<td>#" + rideId + "</td></tr>"
                + "<tr><td><b>Dropped at</b></td>"
                + "<td>" + dropoffAddress + "</td></tr>"
                + "<tr><td><b>Fare</b></td>"
                + "<td>Rs " + fare + "</td></tr>"
                + "</table>"
                + "<p>Please rate your driver in the app.</p>"
                + "<br><p>Thanks,<br><b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(riderEmail, subject, body);
    }

    // Email 4 — sent to DRIVER when ride completes
    @Async
    public void sendRideCompletedEmailToDriver(
            String driverEmail,
            String driverName,
            Long rideId,
            String fare) {

        String subject = appName + " — Ride Completed! Ride #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + driverName + ",</h2>"
                + "<p>You have successfully completed a ride!</p>"
                + "<table border='1' cellpadding='8' "
                + "style='border-collapse:collapse;'>"
                + "<tr><td><b>Ride ID</b></td>"
                + "<td>#" + rideId + "</td></tr>"
                + "<tr><td><b>Fare Earned</b></td>"
                + "<td>Rs " + fare + "</td></tr>"
                + "</table>"
                + "<p>Keep up the great work!</p>"
                + "<br><p>Thanks,<br><b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(driverEmail, subject, body);
    }

    // Email 5 — sent to RIDER when ride is cancelled
    @Async
    public void sendRideCancelledEmailToRider(
            String riderEmail,
            String riderName,
            Long rideId,
            String reason) {

        String subject = appName + " — Ride Cancelled. Ride #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + riderName + ",</h2>"
                + "<p>Your ride has been cancelled.</p>"
                + "<table border='1' cellpadding='8' "
                + "style='border-collapse:collapse;'>"
                + "<tr><td><b>Ride ID</b></td>"
                + "<td>#" + rideId + "</td></tr>"
                + "<tr><td><b>Reason</b></td>"
                + "<td>" + reason + "</td></tr>"
                + "</table>"
                + "<p>Please book a new ride from the app.</p>"
                + "<br><p>Sorry for the inconvenience.<br>"
                + "<b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(riderEmail, subject, body);
    }

    // Email 6 — sent to DRIVER when ride is cancelled
    @Async
    public void sendRideCancelledEmailToDriver(
            String driverEmail,
            String driverName,
            Long rideId) {

        String subject = appName + " — Ride Cancelled. Ride #" + rideId;

        String body = "<html><body>"
                + "<h2>Hello " + driverName + ",</h2>"
                + "<p>The ride #" + rideId
                + " has been cancelled by the rider.</p>"
                + "<p>You are now available for new rides.</p>"
                + "<br><p>Thanks,<br><b>" + appName + " Team</b></p>"
                + "</body></html>";

        sendEmail(driverEmail, subject, body);
    }

    // ──────────────────────────────────────────────────────
    // PRIVATE — Core method that actually sends the email
    // All methods above call this one
    // ──────────────────────────────────────────────────────
    private void sendEmail(String toEmail,
                            String subject,
                            String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // MimeMessageHelper helps build HTML emails easily
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML email

            mailSender.send(message);

            log.info("Email sent to {} — subject: {}",
                    toEmail, subject);

        } catch (MessagingException e) {
            // We log the error but do NOT throw it
            // Email failure should NEVER crash the main application
            log.error("Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}