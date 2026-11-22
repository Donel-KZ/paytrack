-- Academic-term metadata is stored on each invoice line so a single invoice can
-- contain fees for multiple terms while retaining the issued line-item snapshot.
ALTER TABLE invoice_items
    ADD COLUMN academic_term VARCHAR(100) NOT NULL DEFAULT 'General',
    ADD COLUMN academic_term_order INTEGER NOT NULL DEFAULT 0;

ALTER TABLE invoice_items
    ADD CONSTRAINT ck_invoice_items_academic_term_order
        CHECK (academic_term_order >= 0);

CREATE INDEX ix_invoice_items_invoice_term
    ON invoice_items (organisation_id, invoice_id, academic_term_order, academic_term);
