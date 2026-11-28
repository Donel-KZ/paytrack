package org.paytrack.com.invoicing.service;

import org.paytrack.com.invoicing.domain.NumberSeries;
import org.paytrack.com.invoicing.repository.NumberSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InvoiceNumberService {

    public static final String DEFAULT_SERIES_KEY = "DEFAULT";
    public static final String DEFAULT_PREFIX = "INV/";
    public static final int DEFAULT_PADDING = 6;

    private final NumberSeriesRepository numberSeriesRepository;

    public InvoiceNumberService(NumberSeriesRepository numberSeriesRepository) {
        this.numberSeriesRepository = numberSeriesRepository;
    }

    /**
     * Generates the next consecutive invoice number for an organisation and financial year
     * with pessimistic row locking to prevent gaps and duplicates under concurrency.
     */
    @Transactional
    public String generateNextNumber(UUID organisationId, String financialYear, String seriesKey) {
        String key = (seriesKey != null && !seriesKey.isBlank()) ? seriesKey : DEFAULT_SERIES_KEY;

        // 1. Acquire pessimistic write lock on the series row
        NumberSeries series = numberSeriesRepository.findForUpdate(organisationId, financialYear, key)
            .orElseGet(() -> {
                ensureSeriesInitialized(organisationId, financialYear, key, DEFAULT_PREFIX, DEFAULT_PADDING);
                return numberSeriesRepository.findForUpdate(organisationId, financialYear, key)
                    .orElseThrow(() -> new IllegalStateException("Failed to acquire series lock for " + financialYear));
            });

        // 2. Increment atomically
        long nextVal = series.getCurrentValue() + 1;
        series.setCurrentValue(nextVal);
        numberSeriesRepository.saveAndFlush(series);

        // 3. Format invoice number (e.g., INV/2026-27/000001)
        return formatInvoiceNumber(series.getPrefix(), financialYear, series.getPadding(), nextVal);
    }

    @Transactional
    public String generateNextNumber(UUID organisationId, String financialYear) {
        return generateNextNumber(organisationId, financialYear, DEFAULT_SERIES_KEY);
    }

    /**
     * Initializes or configures a series with custom prefix and padding.
     */
    public synchronized NumberSeries ensureSeriesInitialized(
            UUID organisationId,
            String financialYear,
            String seriesKey,
            String prefix,
            int padding) {
        return numberSeriesRepository.findByOrganisationIdAndFinancialYearAndSeriesKey(organisationId, financialYear, seriesKey)
            .orElseGet(() -> {
                NumberSeries newSeries = new NumberSeries(
                    organisationId,
                    financialYear,
                    seriesKey,
                    (prefix != null && !prefix.isBlank()) ? prefix : DEFAULT_PREFIX,
                    0L,
                    padding > 0 ? padding : DEFAULT_PADDING
                );
                return numberSeriesRepository.saveAndFlush(newSeries);
            });
    }

    private String formatInvoiceNumber(String prefix, String financialYear, int padding, long value) {
        String cleanPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        return String.format("%s%s/%0" + padding + "d", cleanPrefix, financialYear, value);
    }
}
