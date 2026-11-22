package com.paytrack.database;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class FlywaySchemaTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("paytrack_test")
            .withUsername("paytrack")
            .withPassword("paytrack");

    @Test
    void allMigrationsApplyAndTenantTablesCarryOrganisationId() throws Exception {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        // organisations is the tenant root and users is a global identity.
        // All organisation-owned business/security tables must carry organisation_id.
        List<String> tenantTables = List.of(
                "organisation_memberships", "roles", "permissions", "role_permissions",
                "refresh_tokens", "payment_terms", "customers", "customer_addresses",
                "tax_rates", "products", "invoice_series", "invoices", "invoice_items",
                "credit_notes", "credit_note_items", "recurring_invoice_templates",
                "recurring_invoice_items", "recurring_invoice_runs", "invoice_documents",
                "audit_logs", "idempotency_records"
        );

        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {
            for (String column : List.of(
                    "early_payment_discount_amount",
                    "early_payment_discount_applied_amount",
                    "early_payment_discount_applied_at"
            )) {
                try (ResultSet result = statement.executeQuery("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'invoices'
                              AND column_name = '%s'
                        )
                        """.formatted(column))) {
                    result.next();
                    assertTrue(result.getBoolean(1),
                            () -> "invoices must contain " + column);
                }
            }

            for (String column : List.of("academic_term", "academic_term_order")) {
                try (ResultSet result = statement.executeQuery("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'invoice_items'
                              AND column_name = '%s'
                        )
                        """.formatted(column))) {
                    result.next();
                    assertTrue(result.getBoolean(1),
                            () -> "invoice_items must contain " + column);
                }
            }

            for (String table : tenantTables) {
                try (ResultSet result = statement.executeQuery("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = '%s'
                              AND column_name = 'organisation_id'
                        )
                        """.formatted(table))) {
                    result.next();
                    assertTrue(result.getBoolean(1),
                            () -> table + " must contain organisation_id");
                }
            }
        }
    }
}
