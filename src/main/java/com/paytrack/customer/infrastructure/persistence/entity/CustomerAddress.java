package com.paytrack.customer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@NoArgsConstructor
public class CustomerAddress extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id", nullable = false) private Customer customer;

    @Column(name = "address_type", nullable = false, length = 30) private String addressType;

    @Column(name = "address_line_1", nullable = false, length = 200) private String addressLine1;

    @Column(name = "address_line_2", length = 200) private String addressLine2;

    @Column(nullable = false, length = 100) private String city;

    @Column(nullable = false, length = 100) private String state;

    @Column(name = "state_code", length = 10) private String stateCode;

    @Column(name = "postal_code", length = 20) private String postalCode;

    @Column(nullable = false, length = 80) private String country;

    @Column(name = "is_default", nullable = false) private boolean defaultAddress;
}
