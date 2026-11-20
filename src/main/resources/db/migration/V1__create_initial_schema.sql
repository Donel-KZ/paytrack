-- PayTrack initial PostgreSQL schema
-- Managed by Flyway. JPA entities map to these tables; Hibernate validates mappings.
-- Tenant-owned references use composite foreign keys so a row cannot point across organisations.

CREATE TABLE organisations (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    legal_name VARCHAR(200),
    registration_number VARCHAR(80),
    tax_registration_number VARCHAR(80),
    email VARCHAR(180),
    phone_number VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(40),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE organisation_memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    organisation_id UUID NOT NULL,
    role_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    joined_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_membership_user_organisation UNIQUE (user_id, organisation_id),
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_memberships_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_memberships_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE payment_terms (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    number_of_days INTEGER NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_payment_terms_org_name UNIQUE (organisation_id, name),
    CONSTRAINT uq_payment_terms_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_payment_terms_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT ck_payment_terms_days CHECK (number_of_days >= 0)
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    payment_terms_id UUID,
    customer_code VARCHAR(60) NOT NULL,
    business_name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(200),
    gstin VARCHAR(20),
    email VARCHAR(254),
    phone_number VARCHAR(40),
    credit_limit NUMERIC(19,2),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customers_org_code UNIQUE (organisation_id, customer_code),
    CONSTRAINT uq_customers_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_customers_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_customers_payment_terms FOREIGN KEY (organisation_id, payment_terms_id)
        REFERENCES payment_terms(organisation_id, id),
    CONSTRAINT ck_customers_credit_limit CHECK (credit_limit IS NULL OR credit_limit >= 0)
);

CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    address_type VARCHAR(30) NOT NULL,
    address_line_1 VARCHAR(200) NOT NULL,
    address_line_2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    state_code VARCHAR(10),
    postal_code VARCHAR(20),
    country VARCHAR(80) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_customer_addresses_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (organisation_id, customer_id)
        REFERENCES customers(organisation_id, id)
);
CREATE UNIQUE INDEX uq_customer_one_default_address
    ON customer_addresses (organisation_id, customer_id) WHERE is_default = TRUE;

CREATE TABLE tax_rates (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    rate NUMERIC(7,4) NOT NULL,
    tax_type VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_tax_rates_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_tax_rates_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT ck_tax_rates_rate CHECK (rate >= 0 AND rate <= 100),
    CONSTRAINT ck_tax_rates_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    tax_rate_id UUID,
    product_code VARCHAR(60) NOT NULL,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    unit_price NUMERIC(19,4) NOT NULL,
    unit_of_measure VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_products_org_code UNIQUE (organisation_id, product_code),
    CONSTRAINT uq_products_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_products_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_products_tax_rate FOREIGN KEY (organisation_id, tax_rate_id)
        REFERENCES tax_rates(organisation_id, id),
    CONSTRAINT ck_products_unit_price CHECK (unit_price >= 0)
);

CREATE TABLE invoice_series (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    series_name VARCHAR(80) NOT NULL,
    prefix VARCHAR(30),
    financial_year VARCHAR(20) NOT NULL,
    next_number BIGINT NOT NULL DEFAULT 1,
    number_padding INTEGER NOT NULL DEFAULT 6,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_invoice_series_org_fy_name UNIQUE (organisation_id, financial_year, series_name),
    CONSTRAINT uq_invoice_series_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_invoice_series_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT ck_invoice_series_next_number CHECK (next_number > 0),
    CONSTRAINT ck_invoice_series_padding CHECK (number_padding > 0)
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    invoice_series_id UUID NOT NULL,
    created_by UUID NOT NULL,
    invoice_number VARCHAR(80),
    financial_year VARCHAR(20) NOT NULL,
    issue_date DATE,
    due_date DATE,
    subtotal NUMERIC(19,2) NOT NULL DEFAULT 0,
    discount_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    cgst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    sgst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    igst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    issued_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_invoices_org_invoice_number UNIQUE (organisation_id, invoice_number),
    CONSTRAINT uq_invoices_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_invoices_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_invoices_customer FOREIGN KEY (organisation_id, customer_id) REFERENCES customers(organisation_id, id),
    CONSTRAINT fk_invoices_series FOREIGN KEY (organisation_id, invoice_series_id) REFERENCES invoice_series(organisation_id, id),
    CONSTRAINT fk_invoices_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ck_invoices_amounts CHECK (subtotal >= 0 AND discount_total >= 0 AND total_amount >= 0),
    CONSTRAINT ck_invoices_due_date CHECK (due_date IS NULL OR issue_date IS NULL OR due_date >= issue_date)
);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    product_id UUID,
    description VARCHAR(500) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    tax_rate NUMERIC(7,4) NOT NULL,
    taxable_amount NUMERIC(19,2) NOT NULL,
    cgst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    sgst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    igst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_invoice_items_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (organisation_id, invoice_id) REFERENCES invoices(organisation_id, id),
    CONSTRAINT fk_invoice_items_product FOREIGN KEY (organisation_id, product_id) REFERENCES products(organisation_id, id),
    CONSTRAINT ck_invoice_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_invoice_items_amounts CHECK (unit_price >= 0 AND discount_amount >= 0 AND tax_rate >= 0)
);

CREATE TABLE credit_notes (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    created_by UUID NOT NULL,
    credit_note_number VARCHAR(80),
    reason VARCHAR(1000),
    issue_date DATE,
    subtotal NUMERIC(19,2) NOT NULL DEFAULT 0,
    cgst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    sgst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    igst_total NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    issued_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_credit_notes_org_number UNIQUE (organisation_id, credit_note_number),
    CONSTRAINT uq_credit_notes_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_credit_notes_invoice FOREIGN KEY (organisation_id, invoice_id) REFERENCES invoices(organisation_id, id),
    CONSTRAINT fk_credit_notes_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE credit_note_items (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    credit_note_id UUID NOT NULL,
    invoice_item_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    tax_rate NUMERIC(7,4) NOT NULL,
    taxable_amount NUMERIC(19,2) NOT NULL,
    cgst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    sgst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    igst_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    line_total NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_credit_note_items_credit_note FOREIGN KEY (organisation_id, credit_note_id) REFERENCES credit_notes(organisation_id, id),
    CONSTRAINT fk_credit_note_items_invoice_item FOREIGN KEY (organisation_id, invoice_item_id) REFERENCES invoice_items(organisation_id, id),
    CONSTRAINT ck_credit_note_items_quantity CHECK (quantity > 0)
);

CREATE TABLE recurring_invoice_templates (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    created_by UUID NOT NULL,
    template_name VARCHAR(160) NOT NULL,
    frequency VARCHAR(30) NOT NULL,
    issue_day INTEGER NOT NULL,
    next_issue_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_recurring_templates_org_id UNIQUE (organisation_id, id),
    CONSTRAINT fk_recurring_templates_customer FOREIGN KEY (organisation_id, customer_id) REFERENCES customers(organisation_id, id),
    CONSTRAINT fk_recurring_templates_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT ck_recurring_template_issue_day CHECK (issue_day BETWEEN 1 AND 31),
    CONSTRAINT ck_recurring_template_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE recurring_invoice_items (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    template_id UUID NOT NULL,
    product_id UUID,
    description VARCHAR(500) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    tax_rate NUMERIC(7,4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_recurring_items_template FOREIGN KEY (organisation_id, template_id) REFERENCES recurring_invoice_templates(organisation_id, id),
    CONSTRAINT fk_recurring_items_product FOREIGN KEY (organisation_id, product_id) REFERENCES products(organisation_id, id),
    CONSTRAINT ck_recurring_items_quantity CHECK (quantity > 0)
);

CREATE TABLE recurring_invoice_runs (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    template_id UUID NOT NULL,
    invoice_id UUID,
    scheduled_date DATE NOT NULL,
    execution_started_at TIMESTAMPTZ,
    execution_completed_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_recurring_run_template_date UNIQUE (template_id, scheduled_date),
    CONSTRAINT fk_recurring_runs_template FOREIGN KEY (organisation_id, template_id) REFERENCES recurring_invoice_templates(organisation_id, id),
    CONSTRAINT fk_recurring_runs_invoice FOREIGN KEY (organisation_id, invoice_id) REFERENCES invoices(organisation_id, id),
    CONSTRAINT ck_recurring_runs_attempt_count CHECK (attempt_count >= 0)
);

CREATE TABLE invoice_documents (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    document_version INTEGER NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_invoice_document_version UNIQUE (invoice_id, document_version),
    CONSTRAINT fk_invoice_documents_invoice FOREIGN KEY (organisation_id, invoice_id) REFERENCES invoices(organisation_id, id),
    CONSTRAINT ck_invoice_documents_size CHECK (file_size >= 0),
    CONSTRAINT ck_invoice_documents_version CHECK (document_version > 0)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_logs_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    response_status INTEGER,
    response_body JSONB,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_idempotency_org_key UNIQUE (organisation_id, idempotency_key),
    CONSTRAINT fk_idempotency_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Common lookup indexes for tenant-scoped access and operational queries.
CREATE INDEX ix_memberships_user ON organisation_memberships(user_id);
CREATE INDEX ix_memberships_organisation ON organisation_memberships(organisation_id);
CREATE INDEX ix_customers_org_status ON customers(organisation_id, status);
CREATE INDEX ix_invoices_org_customer ON invoices(organisation_id, customer_id);
CREATE INDEX ix_invoices_org_status_due ON invoices(organisation_id, status, due_date);
CREATE INDEX ix_invoice_items_invoice ON invoice_items(organisation_id, invoice_id);
CREATE INDEX ix_credit_notes_invoice ON credit_notes(organisation_id, invoice_id);
CREATE INDEX ix_recurring_templates_due ON recurring_invoice_templates(status, next_issue_date);
CREATE INDEX ix_recurring_runs_status_date ON recurring_invoice_runs(status, scheduled_date);
CREATE INDEX ix_audit_logs_org_created ON audit_logs(organisation_id, created_at DESC);
CREATE INDEX ix_refresh_tokens_user_expiry ON refresh_tokens(user_id, expires_at);
CREATE INDEX ix_idempotency_expiry ON idempotency_records(expires_at);
