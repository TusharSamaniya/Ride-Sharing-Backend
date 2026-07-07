package com.rideshare.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        // OneToOne: one User → one Driver profile
    // @JoinColumn creates "user_id" FK column in drivers table
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false)
    private Boolean isAvailable;  // driver toggles this on/off

    @Column(nullable = false)
    private Boolean isVerified = false;   // admin verifies documents

        // Real-time GPS position — updated every few seconds via WebSocket (Phase 05)
    private Double currentLatitude;
    private Double currentLongitude;

    @Column(columnDefinition = "NUMERIC(3,2) DEFAULT 0.00")
    private Double averageRating = 0.0;   // recalculated after every rating

    @Column(nullable = false)
    private Integer totalRides = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}