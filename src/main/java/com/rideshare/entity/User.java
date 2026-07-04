package com.rideshare.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")       // Table name is "users" not "user" (reserved word in SQL)
@Getter
@Setter
@NoArgsConstructor            // JPA requires a no-arg constructor — Lombok generates it
@AllArgsConstructor
@Builder                      // lets you do User.builder().email("x").build()
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        // IDENTITY = PostgreSQL BIGSERIAL (auto-increment: 1, 2, 3 ...)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;        // will be BCrypt hashed in Phase 03

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)    // stores "RIDER" / "DRIVER" as text, not 0/1
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 255)
    private String profilePictureUrl;

    @CreationTimestamp                  // auto-set when record is first saved
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp                    // auto-updated every time record changes
    private LocalDateTime updatedAt;
}