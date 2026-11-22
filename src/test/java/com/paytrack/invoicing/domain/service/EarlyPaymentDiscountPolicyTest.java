package com.paytrack.invoicing.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EarlyPaymentDiscountPolicyTest {

    private final EarlyPaymentDiscountPolicy policy = new EarlyPaymentDiscountPolicy();
    private final LocalDate dueDate = LocalDate.of(2026, 12, 15);
    private final BigDecimal offeredDiscount = new BigDecimal("125.00");

    @Test
    void shouldApplyEarlyPaymentDiscountOnlyWhenInvoiceIsPaidByDueDate() {
        var beforeDueDate = policy.evaluate(
                offeredDiscount, dueDate, dueDate.minusDays(1), true);
        var onDueDate = policy.evaluate(offeredDiscount, dueDate, dueDate, true);
        var afterDueDate = policy.evaluate(
                offeredDiscount, dueDate, dueDate.plusDays(1), true);

        assertTrue(beforeDueDate.eligible());
        assertEquals(offeredDiscount, beforeDueDate.appliedAmount());
        assertTrue(onDueDate.eligible());
        assertEquals(offeredDiscount, onDueDate.appliedAmount());
        assertFalse(afterDueDate.eligible());
        assertEquals(new BigDecimal("0.00"), afterDueDate.appliedAmount());
    }

    @Test
    void appliesDiscountWhenInvoiceIsFullyPaidBeforeDueDate() {
        var decision = policy.evaluate(
                offeredDiscount, dueDate, LocalDate.of(2026, 12, 10), true);

        assertTrue(decision.eligible());
        assertEquals(new BigDecimal("125.00"), decision.appliedAmount());
    }

    @Test
    void appliesDiscountWhenInvoiceIsFullyPaidOnDueDate() {
        var decision = policy.evaluate(offeredDiscount, dueDate, dueDate, true);

        assertTrue(decision.eligible());
        assertEquals(offeredDiscount, decision.appliedAmount());
    }

    @Test
    void doesNotApplyDiscountWhenFullyPaidAfterDueDate() {
        var decision = policy.evaluate(
                offeredDiscount, dueDate, LocalDate.of(2026, 12, 16), true);

        assertFalse(decision.eligible());
        assertEquals(new BigDecimal("0.00"), decision.appliedAmount());
    }

    @Test
    void doesNotApplyDiscountWhenOnlyPartiallyPaidByDueDate() {
        var decision = policy.evaluate(
                offeredDiscount, dueDate, dueDate, false);

        assertFalse(decision.eligible());
        assertEquals(new BigDecimal("0.00"), decision.appliedAmount());
    }

    @Test
    void doesNotApplyDiscountWhenDueDateOrFullPaymentDateIsMissing() {
        assertFalse(policy.evaluate(offeredDiscount, null, dueDate, true).eligible());
        assertFalse(policy.evaluate(offeredDiscount, dueDate, null, true).eligible());
    }

    @Test
    void zeroDiscountIsNeverMarkedAsEarned() {
        var decision = policy.evaluate(BigDecimal.ZERO, dueDate, dueDate, true);

        assertFalse(decision.eligible());
        assertEquals(new BigDecimal("0.00"), decision.appliedAmount());
    }

    @Test
    void rejectsNegativeDiscountOffers() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.evaluate(new BigDecimal("-1.00"), dueDate, dueDate, true));
    }
}
