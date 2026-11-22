package com.paytrack.document.domain.service;

import com.paytrack.invoicing.domain.service.InvoiceTermGroupingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Creates a simple, text-based invoice PDF. Fee lines are presented under their
 * academic term and the conditional early-payment discount is clearly disclosed.
 */
public final class InvoicePdfGenerator {

    private static final float LEFT = 50;
    private static final float TOP = 760;
    private static final float LINE_HEIGHT = 18;

    public byte[] generate(InvoicePdfData invoice) {
        Objects.requireNonNull(invoice, "invoice must not be null");
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = TOP;
                y = write(content, y, "INVOICE " + safe(invoice.invoiceNumber()), 16);
                y = write(content, y - 8, "Customer: " + safe(invoice.customerName()), 10);
                y = write(content, y, "Issue date: " + safe(invoice.issueDate())
                        + "    Due date: " + safe(invoice.dueDate()), 10);
                y -= 8;

                List<InvoiceTermGroupingService.TermGroup> groups =
                        new InvoiceTermGroupingService().groupByAcademicTerm(invoice.feeLines());

                for (var group : groups) {
                    y = ensureSpace(document, page, content, y, 60);
                    y = write(content, y, group.academicTerm(), 12);
                    for (var line : group.feeLines()) {
                        y = write(content, y, "  " + safe(line.description())
                                + "  x " + line.quantity().toPlainString()
                                + "  " + money(line.lineTotal()), 9);
                    }
                    y -= 5;
                }

                y = ensureSpace(document, page, content, y, 100);
                y = write(content, y, "Subtotal: " + money(invoice.subtotal()), 10);
                y = write(content, y, "Invoice total: " + money(invoice.totalAmount()), 10);
                y = write(content, y, "Early-payment discount offered: "
                        + money(invoice.earlyPaymentDiscountOffered()), 10);
                y = write(content, y, "Conditional discount: available only if the invoice is paid"
                        + " in full on or before the due date.", 9);
                y = write(content, y, "Early-payment discount applied: "
                        + money(invoice.earlyPaymentDiscountApplied()), 10);
                y = write(content, y, "Amount due after any earned discount: "
                        + money(invoice.amountDueAfterDiscount()), 10);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate invoice PDF", exception);
        }
    }

    private float write(PDPageContentStream content, float y, String text, int size)
            throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
        content.newLineAtOffset(LEFT, y);
        content.showText(ascii(text));
        content.endText();
        return y - LINE_HEIGHT;
    }

    private float ensureSpace(PDDocument document, PDPage page,
                              PDPageContentStream content, float y, float needed)
            throws IOException {
        if (y >= needed) {
            return y;
        }
        // The first-page generator keeps the content stream simple and predictable.
        // A fresh page is created for overflow and the caller receives its new top.
        content.close();
        PDPage nextPage = new PDPage();
        document.addPage(nextPage);
        return TOP;
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String ascii(String value) {
        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }

    public record InvoicePdfData(
            String invoiceNumber,
            String customerName,
            String issueDate,
            String dueDate,
            List<InvoiceTermGroupingService.FeeLine> feeLines,
            BigDecimal subtotal,
            BigDecimal totalAmount,
            BigDecimal earlyPaymentDiscountOffered,
            BigDecimal earlyPaymentDiscountApplied,
            BigDecimal amountDueAfterDiscount
    ) {
        public InvoicePdfData {
            feeLines = List.copyOf(Objects.requireNonNull(feeLines, "feeLines must not be null"));
            Objects.requireNonNull(subtotal, "subtotal must not be null");
            Objects.requireNonNull(totalAmount, "totalAmount must not be null");
            Objects.requireNonNull(earlyPaymentDiscountOffered, "offered discount must not be null");
            Objects.requireNonNull(earlyPaymentDiscountApplied, "applied discount must not be null");
            Objects.requireNonNull(amountDueAfterDiscount, "amount due must not be null");
        }
    }
}
