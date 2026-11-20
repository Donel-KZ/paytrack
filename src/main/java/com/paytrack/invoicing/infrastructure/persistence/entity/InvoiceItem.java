package com.paytrack.invoicing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_id", nullable = false) private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") private com.paytrack.catalogue.infrastructure.persistence.entity.Product product;

    @Column(nullable = false, length = 500) private String description;

    @Column(nullable = false, precision = 19, scale = 4) private java.math.BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) private java.math.BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 7, scale = 4) private java.math.BigDecimal taxRate;

    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal taxableAmount;

    @Column(name = "cgst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal cgstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "sgst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal sgstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "igst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal igstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal lineTotal;
}
