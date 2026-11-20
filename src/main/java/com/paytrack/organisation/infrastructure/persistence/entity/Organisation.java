package com.paytrack.organisation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "organisations")
@Getter
@Setter
@NoArgsConstructor
public class Organisation extends BaseEntity {
    @Column(nullable = false, length = 160) private String name;

    @Column(name = "legal_name", length = 200) private String legalName;

    @Column(name = "registration_number", length = 80) private String registrationNumber;

    @Column(name = "tax_registration_number", length = 80) private String taxRegistrationNumber;

    @Column(length = 180) private String email;

    @Column(name = "phone_number", length = 40) private String phoneNumber;
}
