package com.paytrack.document.domain.service;

import com.paytrack.invoicing.domain.service.InvoiceTermGroupingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfGeneratorTest {

    @Test
    void generatedPdfDisplaysFeeLinesUnderTheirAcademicTermsAndDiscount() throws Exception {
        var lines = List.of(
                new InvoiceTermGroupingService.FeeLine("Term 2", 2, "Term 2 tuition",
                        BigDecimal.ONE, new BigDecimal("2500.00"), new BigDecimal("2500.00")),
                new InvoiceTermGroupingService.FeeLine("Term 1", 1, "Term 1 tuition",
                        BigDecimal.ONE, new BigDecimal("2500.00"), new BigDecimal("2500.00"))
        );
        var data = new InvoicePdfGenerator.InvoicePdfData(
                "OHA-2026-001", "Ostrava Hillcrest Academy", "2026-01-01", "2026-02-01",
                lines, new BigDecimal("5000.00"), new BigDecimal("5000.00"),
                new BigDecimal("250.00"), BigDecimal.ZERO, new BigDecimal("5000.00"));

        byte[] pdf = new InvoicePdfGenerator().generate(data);

        assertTrue(pdf.length > 0);
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf))) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Term 1"));
            assertTrue(text.contains("Term 1 tuition"));
            assertTrue(text.contains("Term 2"));
            assertTrue(text.contains("Term 2 tuition"));
            assertTrue(text.contains("Early-payment discount offered: 250.00"));
            assertTrue(text.contains("paid in full on or before the due date"));
            assertTrue(text.contains("Early-payment discount applied: 0.00"));
        }
    }
}
