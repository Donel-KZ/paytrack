package org.paytrack.com.invoicing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.paytrack.com.invoicing.service.InvoiceNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InvoiceNumberSeriesConcurrencyTest {

    @Autowired
    private InvoiceNumberService invoiceNumberService;

    @Test
    @DisplayName("A test fires 20 parallel issue requests and all 20 succeed, running from n to n+19 with no gap and no duplicate")
    void testTwentyParallelIssuesGenerateTwentyConsecutiveNumbers() throws Exception {
        UUID orgId = UUID.randomUUID();
        String financialYear = "2026-27";
        String seriesKey = "DEFAULT";
        int threadCount = 20;

        // Ensure series is initialized before firing threads
        invoiceNumberService.ensureSeriesInitialized(orgId, financialYear, seriesKey, "INV/", 6);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        ConcurrentLinkedQueue<String> generatedNumbers = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    // All 20 threads wait here until the latch is released, firing simultaneously
                    startLatch.await();
                    String number = invoiceNumberService.generateNextNumber(orgId, financialYear, seriesKey);
                    generatedNumbers.add(number);
                } catch (Throwable t) {
                    errors.add(t);
                }
            }));
        }

        // Wait for all 20 threads to reach the gate
        assertTrue(readyLatch.await(5, TimeUnit.SECONDS), "All threads should be ready within 5 seconds");
        // Fire all 20 threads simultaneously
        startLatch.countDown();

        // Wait for all 20 tasks to finish
        for (Future<?> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // Acceptance Criterion 1: All 20 parallel issue requests succeed
        assertTrue(errors.isEmpty(), "All 20 requests must succeed with no errors: " + errors);
        assertEquals(20, generatedNumbers.size(), "All 20 requests must produce a number");

        // Acceptance Criterion 2: No duplicates (exactly 20 unique numbers)
        Set<String> uniqueNumbers = new HashSet<>(generatedNumbers);
        assertEquals(20, uniqueNumbers.size(), "Must contain 20 unique numbers. Duplicates detected: " + generatedNumbers);

        // Acceptance Criterion 3: Consecutive numbers from n to n+19 with no gaps
        List<Long> sequenceValues = generatedNumbers.stream()
            .map(num -> {
                assertTrue(num.startsWith("INV/2026-27/"), "Number must match expected prefix format: " + num);
                String[] parts = num.split("/");
                return Long.parseLong(parts[parts.length - 1]);
            })
            .sorted()
            .collect(Collectors.toList());

        long start = sequenceValues.get(0);
        for (int i = 0; i < threadCount; i++) {
            long expected = start + i;
            long actual = sequenceValues.get(i);
            assertEquals(expected, actual, "Sequence must be consecutive with no gaps at index " + i);
        }
        assertEquals(start + 19, sequenceValues.get(threadCount - 1), "Last number must be exactly n + 19");
    }

    @Test
    @DisplayName("A new financial year starts the series again from 1 as set in the design document")
    void testNewFinancialYearResetsSeries() {
        UUID orgId = UUID.randomUUID();

        // Generate numbers for FY 2026-27
        String num1 = invoiceNumberService.generateNextNumber(orgId, "2026-27");
        String num2 = invoiceNumberService.generateNextNumber(orgId, "2026-27");

        assertEquals("INV/2026-27/000001", num1);
        assertEquals("INV/2026-27/000002", num2);

        // Switch to next financial year 2027-28 -> Resets series back to 000001
        String newFyNum1 = invoiceNumberService.generateNextNumber(orgId, "2027-28");
        String newFyNum2 = invoiceNumberService.generateNextNumber(orgId, "2027-28");

        assertEquals("INV/2027-28/000001", newFyNum1, "New FY must start the series again from 1");
        assertEquals("INV/2027-28/000002", newFyNum2, "Second number in new FY must be 2");

        // Subsequent issue on old FY continues where it left off
        String oldFyNum3 = invoiceNumberService.generateNextNumber(orgId, "2026-27");
        assertEquals("INV/2026-27/000003", oldFyNum3, "Old FY series must resume without disturbance");
    }

    @Test
    @DisplayName("Different organisations have isolated invoice number sequences")
    void testTenantIsolationOnInvoiceSeries() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        String fy = "2026-27";

        String orgANum1 = invoiceNumberService.generateNextNumber(orgA, fy);
        String orgBNum1 = invoiceNumberService.generateNextNumber(orgB, fy);

        assertEquals("INV/2026-27/000001", orgANum1);
        assertEquals("INV/2026-27/000001", orgBNum1, "Organisation B must have its own independent sequence");
    }
}
