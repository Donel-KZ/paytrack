package com.paytrack.invoicing.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Evaluates a conditional early-payment discount after payment allocation has
 * established whether the invoice is fully settled.
 *
 * The invoice's gross total is not changed by this policy. A qualifying
 * discount is recorded separately as a settlement adjustment.
 */
public final class EarlyPaymentDiscountPolicy {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    public Decision evaluate(
            BigDecimal offeredDiscount,
            LocalDate dueDate,
            LocalDate fullPaymentReceivedDate,
            boolean invoiceFullyPaid
    ) {
        Objects.requireNonNull(offeredDiscount, "offeredDiscount must not be null");

        BigDecimal normalizedOffer = offeredDiscount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedOffer.signum() < 0) {
            throw new IllegalArgumentException("Offered early-payment discount cannot be negative");
        }

        boolean eligible = normalizedOffer.signum() > 0
                && invoiceFullyPaid
                && dueDate != null
                && fullPaymentReceivedDate != null
                && !fullPaymentReceivedDate.isAfter(dueDate);

        return eligible
                ? new Decision(true, normalizedOffer)
                : new Decision(false, ZERO);
    }

    public record Decision(boolean eligible, BigDecimal appliedAmount) {
    }
}
