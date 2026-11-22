-- Persist the conditional early-payment discount offer and the amount actually earned.
-- The offered amount is part of the issued invoice snapshot and must be displayed on its PDF.
-- The applied amount is populated only after payment allocation confirms full settlement by due date.
ALTER TABLE invoices
    ADD COLUMN early_payment_discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    ADD COLUMN early_payment_discount_applied_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    ADD COLUMN early_payment_discount_applied_at TIMESTAMPTZ;

ALTER TABLE invoices
    ADD CONSTRAINT ck_invoices_early_payment_discount_amounts
        CHECK (
            early_payment_discount_amount >= 0
            AND early_payment_discount_applied_amount >= 0
            AND early_payment_discount_applied_amount <= early_payment_discount_amount
        ),
    ADD CONSTRAINT ck_invoices_early_payment_discount_applied_at
        CHECK (
            (early_payment_discount_applied_amount = 0 AND early_payment_discount_applied_at IS NULL)
            OR
            (early_payment_discount_applied_amount > 0 AND early_payment_discount_applied_at IS NOT NULL)
        );
