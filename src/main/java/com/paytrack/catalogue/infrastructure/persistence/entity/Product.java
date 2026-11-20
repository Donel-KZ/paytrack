package com.paytrack.catalogue.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "products", uniqueConstraints = {@UniqueConstraint(name = "uk_products_org_code", columnNames = {"organisation_id", "product_code"})})
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tax_rate_id") private com.paytrack.tax.infrastructure.persistence.entity.TaxRate taxRate;

    @Column(name = "product_code", nullable = false, length = 60) private String productCode;

    @Column(nullable = false, length = 180) private String name;

    @Column(length = 1000) private String description;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) private java.math.BigDecimal unitPrice;

    @Column(name = "unit_of_measure", length = 30) private String unitOfMeasure;

    @Column(name = "is_active", nullable = false) private boolean active = true;
}
