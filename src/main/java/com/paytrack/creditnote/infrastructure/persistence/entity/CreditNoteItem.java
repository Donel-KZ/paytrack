package com.paytrack.creditnote.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "credit_note_items")
@Getter
@Setter
@NoArgsConstructor
public class CreditNoteItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "credit_note_id", nullable = false) private CreditNote creditNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_item_id", nullable = false) private com.paytrack.invoicing.infrastructure.persistence.entity.InvoiceItem invoiceItem;

    @Column(nullable = false, length = 500) private String description;

    @Column(nullable = false, precision = 19, scale = 4) private java.math.BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4) private java.math.BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 7, scale = 4) private java.math.BigDecimal taxRate;

    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal taxableAmount;

    @Column(name = "cgst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal cgstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "sgst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal sgstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "igst_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal igstAmount = java.math.BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal lineTotal;
}
