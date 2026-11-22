package com.paytrack.invoicing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "invoices", uniqueConstraints = {@UniqueConstraint(name = "uk_invoices_org_invoice_number", columnNames = {"organisation_id", "invoice_number"})})
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private com.paytrack.organisation.infrastructure.persistence.entity.Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id", nullable = false) private com.paytrack.customer.infrastructure.persistence.entity.Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_series_id", nullable = false) private com.paytrack.numbering.infrastructure.persistence.entity.InvoiceSeries invoiceSeries;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private com.paytrack.identity.infrastructure.persistence.entity.AppUser createdBy;

    @Column(name = "invoice_number", length = 80) private String invoiceNumber;

    @Column(name = "financial_year", nullable = false, length = 20) private String financialYear;

    @Column(name = "issue_date") private java.time.LocalDate issueDate;

    @Column(name = "due_date") private java.time.LocalDate dueDate;

    @Column(nullable = false, precision = 19, scale = 2) private java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal discountTotal = java.math.BigDecimal.ZERO;
    /**
     * Conditional discount offered on the issued invoice. This is a snapshot for
     * customer communication/PDF display; it is not deducted from totalAmount.
     */
    @Column(name = "early_payment_discount_amount", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal earlyPaymentDiscountAmount = java.math.BigDecimal.ZERO;

    /**
     * Discount actually earned after the invoice is fully settled on or before dueDate.
     * Payment allocation integration is responsible for setting this value.
     */
    @Column(name = "early_payment_discount_applied_amount", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal earlyPaymentDiscountAppliedAmount = java.math.BigDecimal.ZERO;

    @Column(name = "early_payment_discount_applied_at")
    private java.time.Instant earlyPaymentDiscountAppliedAt;


    @Column(name = "cgst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal cgstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "sgst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal sgstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "igst_total", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal igstTotal = java.math.BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

    @Column(nullable = false, length = 3) private String currency;

    @Column(nullable = false, length = 30) private String status;

    @Column(length = 2000) private String notes;

    @Column(name = "issued_at") private java.time.Instant issuedAt;

    @Version @Column(nullable = false) private Long version = 0L;
}
