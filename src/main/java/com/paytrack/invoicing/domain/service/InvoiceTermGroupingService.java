package com.paytrack.invoicing.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Groups invoice fee lines by their academic term for invoice presentation.
 * Term order is explicit so the PDF does not depend on alphabetical ordering.
 */
public final class InvoiceTermGroupingService {

    public List<TermGroup> groupByAcademicTerm(List<FeeLine> feeLines) {
        Objects.requireNonNull(feeLines, "feeLines must not be null");

        Map<TermKey, List<FeeLine>> grouped = new LinkedHashMap<>();
        for (FeeLine line : feeLines) {
            Objects.requireNonNull(line, "feeLines cannot contain null entries");
            String term = line.academicTerm() == null || line.academicTerm().isBlank()
                    ? "General" : line.academicTerm().trim();
            int order = line.academicTermOrder() == null ? 0 : line.academicTermOrder();
            grouped.computeIfAbsent(new TermKey(term, order), ignored -> new ArrayList<>()).add(line);
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        Comparator.comparingInt(TermKey::order).thenComparing(TermKey::name,
                                String.CASE_INSENSITIVE_ORDER)))
                .map(entry -> new TermGroup(entry.getKey().name(), entry.getKey().order(),
                        List.copyOf(entry.getValue())))
                .toList();
    }

    private record TermKey(String name, int order) { }

    public record FeeLine(
            String academicTerm,
            Integer academicTermOrder,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) { }

    public record TermGroup(String academicTerm, int academicTermOrder, List<FeeLine> feeLines) { }
}
