-- Flyway migration: create number_series table with current_value column
CREATE TABLE IF NOT EXISTS number_series (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    series_key VARCHAR(50) NOT NULL,
    prefix VARCHAR(50) NOT NULL DEFAULT 'INV/',
    current_value BIGINT NOT NULL DEFAULT 0,
    padding INT NOT NULL DEFAULT 6,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_number_series_org_fy_key UNIQUE (organisation_id, financial_year, series_key)
);

CREATE INDEX IF NOT EXISTS idx_number_series_lookup 
ON number_series (organisation_id, financial_year, series_key);
