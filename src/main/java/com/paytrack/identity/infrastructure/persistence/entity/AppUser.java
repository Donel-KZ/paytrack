package com.paytrack.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name = "uk_users_email", columnNames = "email")})
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends BaseEntity {
    @Column(nullable = false, length = 120) private String firstName;

    @Column(nullable = false, length = 120) private String lastName;

    @Column(nullable = false, length = 254) private String email;

    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;

    @Column(name = "phone_number", length = 40) private String phoneNumber;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    @Column(name = "email_verified", nullable = false) private boolean emailVerified = false;
}
