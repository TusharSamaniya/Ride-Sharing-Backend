package com.rideshare.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        // ManyToOne: many rides can be taken by one rider
    // Creates "rider_id" FK column in rides table → references users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

        // ManyToOne: many rides can be driven by one driver
    // nullable=true because driver_id is NULL until a driver accepts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

        // ── Pickup location ──────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLongitude;

    @Column(length = 500)
    private String pickupAddress;      // human-readable address string

        // ── Dropoff location ─────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLatitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal dropoffLongitude;

    @Column(length = 500)
    private String dropoffAddress;

        // ── Ride status ──────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideStatus status = RideStatus.REQUESTED;

        // ── Fare ─────────────────────────────────────────────
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedFare;   // shown to rider before booking

    @Column(precision = 10, scale = 2)
    private BigDecimal finalFare;       // calculated after ride ends

    @Column(precision = 8, scale = 2)
    private BigDecimal distanceKm;

    private Integer durationMinutes;

        // ── Timestamps for each status change ───────────────
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime acceptedAt;
    private LocalDateTime driverArrivedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancellationReason;
}