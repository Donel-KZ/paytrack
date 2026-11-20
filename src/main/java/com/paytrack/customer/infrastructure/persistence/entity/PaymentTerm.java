package com.paytrack.customer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "payment_terms", uniqueConstraints = {@UniqueConstraint(name = "uk_payment_terms_org_name", columnNames = {"organisation_id", "name"})})
@Getter
@Setter
@NoArgsConstructor
public class PaymentTerm extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @Column(nullable = false, length = 100) private String name;

    @Column(name = "number_of_days", nullable = false) private Integer numberOfDays;

    @Column(length = 255) private String description;
}
