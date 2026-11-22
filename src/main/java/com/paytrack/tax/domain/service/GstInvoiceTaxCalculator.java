package com.paytrack.tax.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calculates Indian GST per invoice line.
 *
 * Intra-state supply splits the rate equally between CGST and SGST;
 * inter-state supply applies the full rate as IGST. Each tax component is
 * rounded independently to two decimals using HALF_UP before invoice totals
 * are summed.
 */
public final class GstInvoiceTaxCalculator {

    private static final int MONEY_SCALE = 2;
    private static final int CALCULATION_SCALE = 8;
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWO_HUNDRED = new BigDecimal("200");

    public InvoiceTaxResult calculate(InvoiceInput invoice) {
        Objects.requireNonNull(invoice, "invoice must not be null");
        requireText(invoice.supplierStateCode(), "supplierStateCode");
        requireText(invoice.placeOfSupplyStateCode(), "placeOfSupplyStateCode");
        Objects.requireNonNull(invoice.lines(), "invoice lines must not be null");
        if (invoice.lines().isEmpty()) {
            throw new IllegalArgumentException("Invoice must contain at least one tax line");
        }

        boolean intraState = invoice.supplierStateCode().trim()
                .equalsIgnoreCase(invoice.placeOfSupplyStateCode().trim());

        BigDecimal subtotal = ZERO;
        BigDecimal cgstTotal = ZERO;
        BigDecimal sgstTotal = ZERO;
        BigDecimal igstTotal = ZERO;
        BigDecimal invoiceTotal = ZERO;

        var calculatedLines = new java.util.ArrayList<CalculatedTaxLine>();
        for (TaxLineInput line : invoice.lines()) {
            CalculatedTaxLine calculated = calculateLine(line, intraState);
            calculatedLines.add(calculated);
            subtotal = subtotal.add(calculated.taxableAmount());
            cgstTotal = cgstTotal.add(calculated.cgstAmount());
            sgstTotal = sgstTotal.add(calculated.sgstAmount());
            igstTotal = igstTotal.add(calculated.igstAmount());
            invoiceTotal = invoiceTotal.add(calculated.lineTotal());
        }

        return new InvoiceTaxResult(
                money(subtotal), money(cgstTotal), money(sgstTotal), money(igstTotal),
                money(cgstTotal.add(sgstTotal).add(igstTotal)), money(invoiceTotal),
                List.copyOf(calculatedLines)
        );
    }

    private CalculatedTaxLine calculateLine(TaxLineInput line, boolean intraState) {
        Objects.requireNonNull(line, "invoice lines cannot contain null");
        Objects.requireNonNull(line.quantity(), "quantity must not be null");
        Objects.requireNonNull(line.unitPrice(), "unitPrice must not be null");
        Objects.requireNonNull(line.discountAmount(), "discountAmount must not be null");
        Objects.requireNonNull(line.taxRate(), "taxRate must not be null");

        if (line.quantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (line.unitPrice().signum() < 0 || line.discountAmount().signum() < 0) {
            throw new IllegalArgumentException("Unit price and discount cannot be negative");
        }
        if (line.taxRate().signum() < 0 || line.taxRate().compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("GST rate must be between 0 and 100 percent");
        }

        BigDecimal gross = line.quantity().multiply(line.unitPrice())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (line.discountAmount().compareTo(gross) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed the line gross amount");
        }
        BigDecimal taxable = money(gross.subtract(line.discountAmount()));

        BigDecimal cgst = ZERO;
        BigDecimal sgst = ZERO;
        BigDecimal igst = ZERO;
        if (intraState) {
            BigDecimal halfRateTax = taxable.multiply(line.taxRate())
                    .divide(TWO_HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            cgst = halfRateTax;
            sgst = halfRateTax;
        } else {
            igst = taxable.multiply(line.taxRate())
                    .divide(ONE_HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal lineTotal = money(taxable.add(cgst).add(sgst).add(igst));
        return new CalculatedTaxLine(
                line.description(), taxable, cgst, sgst, igst, lineTotal
        );
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    public record InvoiceInput(
            String supplierStateCode,
            String placeOfSupplyStateCode,
            List<TaxLineInput> lines
    ) {
        public InvoiceInput {
            lines = lines == null ? null : List.copyOf(lines);
        }
    }

    public record TaxLineInput(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountAmount,
            BigDecimal taxRate
    ) { }

    public record CalculatedTaxLine(
            String description,
            BigDecimal taxableAmount,
            BigDecimal cgstAmount,
            BigDecimal sgstAmount,
            BigDecimal igstAmount,
            BigDecimal lineTotal
    ) { }

    public record InvoiceTaxResult(
            BigDecimal subtotal,
            BigDecimal cgstTotal,
            BigDecimal sgstTotal,
            BigDecimal igstTotal,
            BigDecimal taxTotal,
            BigDecimal invoiceTotal,
            List<CalculatedTaxLine> lines
    ) {
        public InvoiceTaxResult {
            lines = List.copyOf(lines);
        }
    }
}
