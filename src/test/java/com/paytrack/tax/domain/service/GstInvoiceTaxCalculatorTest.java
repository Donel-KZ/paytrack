package com.paytrack.tax.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GstInvoiceTaxCalculatorTest {

    private final GstInvoiceTaxCalculator calculator = new GstInvoiceTaxCalculator();

    @Test
    void calculatesCgstAndSgstForSameStateSupply() {
        // Hand calculation from docs/design.md, Section 5:
        // Taxable base ₹100.00 at 18% intra-state.
        // CGST = ₹100.00 × 9% = ₹9.00; SGST = ₹100.00 × 9% = ₹9.00.
        // Total tax = ₹18.00; invoice line total = ₹118.00.
        var result = calculator.calculate(new GstInvoiceTaxCalculator.InvoiceInput(
                "27", "27",
                List.of(line("₹100 at 18%", "1", "100.00", "0.00", "18.0000"))
        ));

        assertMoney("100.00", result.subtotal());
        assertMoney("9.00", result.cgstTotal());
        assertMoney("9.00", result.sgstTotal());
        assertMoney("0.00", result.igstTotal());
        assertMoney("18.00", result.taxTotal());
        assertMoney("118.00", result.invoiceTotal());
    }

    @Test
    void calculatesIgstForOtherStateSupply() {
        // Hand calculation from docs/design.md, Section 5:
        // Taxable base ₹100.00 at 18% inter-state.
        // IGST = ₹100.00 × 18% = ₹18.00; invoice line total = ₹118.00.
        var result = calculator.calculate(new GstInvoiceTaxCalculator.InvoiceInput(
                "27", "29",
                List.of(line("₹100 at 18%", "1", "100.00", "0.00", "18.0000"))
        ));

        assertMoney("100.00", result.subtotal());
        assertMoney("0.00", result.cgstTotal());
        assertMoney("0.00", result.sgstTotal());
        assertMoney("18.00", result.igstTotal());
        assertMoney("18.00", result.taxTotal());
        assertMoney("118.00", result.invoiceTotal());
    }

    @Test
    void calculatesMixedTaxRatesPerLineForSameStateSupply() {
        // Hand calculation, using the documented per-line HALF_UP rule:
        // Line 1: ₹100.00 × 18% => CGST ₹9.00 + SGST ₹9.00.
        // Line 2: ₹200.00 × 5%  => CGST ₹5.00 + SGST ₹5.00.
        // Totals: taxable ₹300.00; CGST ₹14.00; SGST ₹14.00;
        // total tax ₹28.00; invoice total ₹328.00.
        var result = calculator.calculate(new GstInvoiceTaxCalculator.InvoiceInput(
                "27", "27",
                List.of(
                        line("₹100 at 18%", "1", "100.00", "0.00", "18.0000"),
                        line("₹200 at 5%", "1", "200.00", "0.00", "5.0000")
                )
        ));

        assertMoney("300.00", result.subtotal());
        assertMoney("14.00", result.cgstTotal());
        assertMoney("14.00", result.sgstTotal());
        assertMoney("0.00", result.igstTotal());
        assertMoney("28.00", result.taxTotal());
        assertMoney("328.00", result.invoiceTotal());
        assertEquals(2, result.lines().size());
        assertMoney("9.00", result.lines().get(0).cgstAmount());
        assertMoney("5.00", result.lines().get(1).cgstAmount());
    }

    private GstInvoiceTaxCalculator.TaxLineInput line(
            String description, String quantity, String unitPrice,
            String discount, String rate) {
        return new GstInvoiceTaxCalculator.TaxLineInput(
                description,
                new BigDecimal(quantity),
                new BigDecimal(unitPrice),
                new BigDecimal(discount),
                new BigDecimal(rate)
        );
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "Expected ₹" + expected + " but got ₹" + actual);
    }
}
