package com.paytrack.tax.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "tax_rates")
@Getter
@Setter
@NoArgsConstructor
public class TaxRate extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @Column(nullable = false, length = 100) private String name;

    @Column(nullable = false, precision = 7, scale = 4) private java.math.BigDecimal rate;

    @Column(name = "tax_type", nullable = false, length = 30) private String taxType;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    @Column(name = "effective_from") private java.time.LocalDate effectiveFrom;

    @Column(name = "effective_to") private java.time.LocalDate effectiveTo;
}
