package com.paytrack.document.domain.service;

import com.paytrack.invoicing.domain.service.InvoiceTermGroupingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Generates an invoice PDF with fee lines grouped under their academic terms. */
public final class InvoicePdfGenerator {

    public byte[] generate(InvoicePdfData invoice) {
        Objects.requireNonNull(invoice, "invoice must not be null");
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             Layout layout = new Layout(document)) {

            layout.write("INVOICE " + safe(invoice.invoiceNumber()), 16);
            layout.write("Customer: " + safe(invoice.customerName()), 10);
            layout.write("Issue date: " + safe(invoice.issueDate())
                    + "    Due date: " + safe(invoice.dueDate()), 10);
            layout.blank();

            var groups = new InvoiceTermGroupingService().groupByAcademicTerm(invoice.feeLines());
            for (var group : groups) {
                layout.write(group.academicTerm(), 12);
                for (var line : group.feeLines()) {
                    layout.write("  " + safe(line.description()) + "  x "
                            + line.quantity().toPlainString() + "  " + money(line.lineTotal()), 9);
                }
                layout.blank();
            }

            layout.write("Subtotal: " + money(invoice.subtotal()), 10);
            layout.write("Invoice total: " + money(invoice.totalAmount()), 10);
            layout.write("Early-payment discount offered: "
                    + money(invoice.earlyPaymentDiscountOffered()), 10);
            layout.write("Conditional discount: available only if the invoice is paid in full"
                    + " on or before the due date.", 9);
            layout.write("Early-payment discount applied: "
                    + money(invoice.earlyPaymentDiscountApplied()), 10);
            layout.write("Amount due after any earned discount: "
                    + money(invoice.amountDueAfterDiscount()), 10);

            // PDFBox cannot serialize a document while a content stream writer is open.
            layout.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate invoice PDF", exception);
        }
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class Layout implements AutoCloseable {
        private static final float LEFT = 50;
        private static final float TOP = 760;
        private static final float LINE_HEIGHT = 18;
        private final PDDocument document;
        private PDPageContentStream content;
        private float y;

        private Layout(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void write(String text, int size) throws IOException {
            if (y < 55) {
                newPage();
            }
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
            content.newLineAtOffset(LEFT, y);
            content.showText(ascii(text));
            content.endText();
            y -= LINE_HEIGHT;
        }

        private void blank() {
            y -= 5;
        }

        private void newPage() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
            document.addPage(new PDPage());
            content = new PDPageContentStream(document,
                    document.getPage(document.getNumberOfPages() - 1));
            y = TOP;
        }

        private static String ascii(String value) {
            return value.replaceAll("[^\\x20-\\x7E]", "?");
        }

        @Override
        public void close() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }
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
