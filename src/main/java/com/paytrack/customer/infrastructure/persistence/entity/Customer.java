package com.paytrack.customer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "customers", uniqueConstraints = {@UniqueConstraint(name = "uk_customers_org_code", columnNames = {"organisation_id", "customer_code"})})
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_terms_id") private PaymentTerm paymentTerms;

    @Column(name = "customer_code", nullable = false, length = 60) private String customerCode;

    @Column(name = "business_name", nullable = false, length = 200) private String businessName;

    @Column(name = "legal_name", length = 200) private String legalName;

    @Column(length = 20) private String gstin;

    @Column(length = 254) private String email;

    @Column(name = "phone_number", length = 40) private String phoneNumber;

    @Column(name = "credit_limit", precision = 19, scale = 2) private java.math.BigDecimal creditLimit;

    @Column(nullable = false, length = 30) private String status;
}
