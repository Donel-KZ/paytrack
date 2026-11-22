package com.paytrack.invoicing.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTermGroupingServiceTest {

    private final InvoiceTermGroupingService groupingService = new InvoiceTermGroupingService();

    @Test
    void shouldGroupFeeLinesByAcademicTerm() {
        var termTwoTuition = fee("Term 2", 2, "Tuition", "2500.00");
        var termOneTuition = fee("Term 1", 1, "Tuition", "2500.00");
        var termOneBooks = fee("Term 1", 1, "Books", "350.00");
        var termThreeTransport = fee("Term 3", 3, "Transport", "600.00");

        var groups = groupingService.groupByAcademicTerm(
                List.of(termTwoTuition, termOneTuition, termOneBooks, termThreeTransport));

        assertEquals(3, groups.size());
        assertEquals(List.of("Term 1", "Term 2", "Term 3"),
                groups.stream().map(InvoiceTermGroupingService.TermGroup::academicTerm).toList());
        assertEquals(List.of("Tuition", "Books"),
                groups.get(0).feeLines().stream()
                        .map(InvoiceTermGroupingService.FeeLine::description).toList());
        assertEquals(2, groups.get(0).feeLines().size());
        assertEquals(1, groups.get(1).feeLines().size());
        assertEquals(1, groups.get(2).feeLines().size());
    }

    private InvoiceTermGroupingService.FeeLine fee(
            String term, int order, String description, String amount) {
        return new InvoiceTermGroupingService.FeeLine(term, order, description,
                BigDecimal.ONE, new BigDecimal(amount), new BigDecimal(amount));
    }
}
