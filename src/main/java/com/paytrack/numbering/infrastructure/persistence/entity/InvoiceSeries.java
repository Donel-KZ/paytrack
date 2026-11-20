package com.paytrack.numbering.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "invoice_series", uniqueConstraints = {@UniqueConstraint(name = "uk_invoice_series_org_fy_name", columnNames = {"organisation_id", "financial_year", "series_name"})})
@Getter
@Setter
@NoArgsConstructor
public class InvoiceSeries extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @Column(name = "series_name", nullable = false, length = 80) private String seriesName;

    @Column(length = 30) private String prefix;

    @Column(name = "financial_year", nullable = false, length = 20) private String financialYear;

    @Column(name = "next_number", nullable = false) private Long nextNumber = 1L;

    @Column(name = "number_padding", nullable = false) private Integer numberPadding = 6;

    @Column(name = "is_active", nullable = false) private boolean active = true;
}
