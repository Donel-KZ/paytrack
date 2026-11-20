package com.paytrack.creditnote.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "credit_notes", uniqueConstraints = {@UniqueConstraint(name = "uk_credit_notes_org_number", columnNames = {"organisation_id", "credit_note_number"})})
@Getter
@Setter
@NoArgsConstructor
public class CreditNote extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_id", nullable = false) private com.paytrack.invoicing.infrastructure.persistence.entity.Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private com.paytrack.identity.infrastructure.persistence.entity.AppUser createdBy;

    @Column(name = "credit_note_number", length = 80) private String creditNoteNumber;

    @Column(length = 1000) private String reason;

    @Column(name = "issue_date") private java.time.LocalDate issueDate;

    @Column(nullable = false, precision = 19, scale = 2) private java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;

    @Column(name = "cgst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal cgstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "sgst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal sgstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "igst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal igstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

    @Column(nullable = false, length = 30) private String status;

    @Column(name = "issued_at") private java.time.Instant issuedAt;
}
