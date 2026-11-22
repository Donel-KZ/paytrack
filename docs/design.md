# PayTrack — Phase 1 Technical Design

**Document:** LV-JAVA-PAYTRACK-P1-TDD  
**Status:** Working draft for mentor review  
**Version:** 0.2  
**Requirements baseline:** LV-JAVA-PAYTRACK-P1 v1.0, issued 12 November 2026  
**Review milestone:** M1.1 — 15 November 2026

> **Design status:** The requirements define the five roles and require a role-by-action permission matrix, but do not prescribe the individual permissions. The matrix below is a proposed least-privilege baseline for mentor approval. The team's additional client-specific rule under “Your client” in the LogicVeda Team Workspace is not included in the PDF; FR-1.08 remains an open requirement and this design must be updated when that rule is retrieved.

## 1. Permission matrix (FR-1.01, NFR-02)

**Legend:** C = create; R = read; U = update; D = delete; I = issue/execute; A = administer; — = not permitted. All permissions apply only within the user's own organisation (tenant).

| Action | Owner | Finance manager | Accountant | Collector | Read-only |
|---|---:|---:|---:|---:|---:|
| View organisation profile/settings | R/U | R | R | — | R |
| Update organisation profile/settings | A | — | — | — | — |
| Invite, deactivate and manage users (excluding owner transfer) | A | R/U* | — | — | — |
| Assign or change roles | A | — | — | — | — |
| View customer records | R | R | R | R | R |
| Create customer | C | C | C | — | — |
| Update customer | U | U | U | — | — |
| Delete customer (only if no financial documents depend on it) | D | D | — | — | — |
| View draft invoices | R | R | R | — | R |
| Create draft invoices | C | C | C | — | — |
| Edit draft invoices | U | U | U | — | — |
| Delete draft invoices | D | D | D | — | — |
| Issue invoices (finalise and allocate number) | I | I | I | — | — |
| View issued invoices and invoice PDFs | R | R | R | R | R |
| Void an issued invoice (retain record and number) | I | I | I | — | — |
| Create credit notes | I | I | I | — | — |
| View credit notes and PDFs | R | R | R | R | R |
| Manage recurring invoice templates | C/R/U/D/I | C/R/U/D/I | R | — | R |
| View customer balances / overdue invoice information | R | R | R | R | R |
| View audit history | R | R | R (financial records) | R (collection-related records) | R |
| Record or allocate payments | —** | —** | —** | —** | —** |

* Finance manager may manage non-owner users only; cannot grant or transfer the owner role.  
** Payment recording and allocation are not assigned in this Phase 1 matrix: the Phase 1 requirements describe payment/collection business objectives, but the team structure places payments and allocation in Engineer C's Phase 2-onward scope. Confirm any client-specific exception with the mentor.

**Enforcement rules**

- Deny by default. Enforce permissions in Spring Security at endpoint/method level; hiding a UI control is not authorization.
- Every protected endpoint must have tests for at least one permitted role and one denied role (NFR-02).
- Scope every permission to the authenticated user's organisation. Never accept a client-supplied organisation ID as proof of access.
- A user from organisation A requesting organisation B's invoice by ID receives 404 (NFR-01).
- Issued invoices and their PDFs are immutable. Do not physically delete an issued invoice; use a credit note for corrections and retain the audit trail.

## 2. Architecture

### 2.1 Proposed structure

Use a **modular monolith** for Phase 1. Keep domain boundaries explicit while placing invoice issue, tax calculation, number allocation and audit recording in one PostgreSQL transaction. This avoids distributed transaction complexity at the project's current scope and leaves modules separable later if needed.

| Layer | Required technology | Responsibility |
|---|---|---|
| Frontend | React 18, TypeScript, Vite, TanStack Query, React Hook Form, Zod, Tailwind | User interface, client-side validation and API calls |
| Backend/API | Java 21, Spring Boot 3 Web, Validation, Security; springdoc OpenAPI | REST controllers, authentication, authorization, validation |
| Application/domain | Layered controllers, services and repositories; identity, customers, invoicing, tax, numbering, documents and audit modules | Use cases and business rules |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate, Flyway | Tenant data, transactions, constraints and migrations |
| Supporting services | Redis; RabbitMQ with Spring AMQP; Spring Scheduler or Quartz | Cache/rate limits; asynchronous jobs and recurring invoice schedule |
| File storage | S3-compatible storage | Store server-generated invoice and credit-note PDFs; issue signed download links |
| Delivery/operations | Docker, docker-compose, GitHub Actions, Render; Actuator/Micrometer | CI/CD, deployment, health/readiness and monitoring |

### 2.2 Request and processing flow

1. React calls a versioned `/api/v1` endpoint.
2. Spring Security authenticates the user and resolves their organisation membership and role.
3. The application service validates the use case and applies the permission and tenant-scope rules.
4. Repositories query/update only records belonging to that organisation.
5. The service commits the business transaction and audit event. Work that can safely happen after commit (for example, notification delivery) may be queued through RabbitMQ.
6. Server-side PDF generation stores the file in S3-compatible storage; the API returns a time-limited signed URL.

Do not use Redis as the source of truth for invoice numbers. Number allocation must be protected by the relational database transaction and lock described below.

## 3. Data model

### 3.1 Core entities

All tenant-owned tables carry a non-null `organisation_id` (NFR-01). Use UUID primary keys, created/updated timestamps, and database constraints. Monetary values use PostgreSQL `NUMERIC` and Java `BigDecimal`, never floating-point types. Store currency with monetary values.

| Entity | Key fields and constraints |
|---|---|
| `organisations` | `id`, legal name, supplier GST/state registration details, financial-year settings, default currency |
| `users` | `id`, email, password hash, account status |
| `organisation_memberships` | `id`, `organisation_id`, `user_id`, role; unique organisation/user pair |
| `customers` | `id`, `organisation_id`, legal name, GSTIN, billing/shipping addresses, state code, payment terms, credit limit |
| `invoice_series` | `id`, `organisation_id`, financial year, series key, prefix, next number, padding |
| `invoices` | `id`, `organisation_id`, customer, series, invoice number, financial year, issue/due dates, place of supply, status, currency, totals, conditional early-payment discount offered/applied amounts and applied timestamp, version |
| `invoice_lines` | `id`, `organisation_id`, invoice, description, quantity, unit price, discount, taxable base, tax rate/components, line total |
| `credit_notes` | `id`, `organisation_id`, linked invoice, credit-note number, reason, issue date, totals and status |
| `credit_note_lines` | `id`, `organisation_id`, credit note, source invoice line, quantity, taxable base, tax components and line total |
| `recurring_invoice_templates` | `id`, `organisation_id`, customer, schedule day, timezone, template data, next run, active flag |
| `audit_events` | `id`, `organisation_id`, actor, entity type/ID, action, timestamp, before/after snapshot or diff |
| `document_files` | `id`, `organisation_id`, entity type/ID, storage key, content type, size and checksum |

### 3.2 Relationships and integrity

- An organisation has many memberships, customers, invoice series, invoices, recurring templates and audit events.
- A customer has many invoices and may have recurring invoice templates.
- An invoice has many invoice lines and may have linked credit notes.
- A credit note has many credit-note lines; each line can refer to the original invoice line it corrects.
- Use tenant-aware foreign keys/constraints where practical so a child row cannot reference a parent from another organisation.
- Add indexes beginning with `organisation_id` for common tenant-scoped list/detail queries.
- Use optimistic locking (for example, a version column) for mutable drafts where concurrent edits are possible. Use Flyway for every schema change.

### 3.3 Early-payment discount

An invoice may carry an optional conditional early-payment discount. The invoice stores the offered amount as an immutable snapshot when the invoice is issued. The invoice PDF must display the offered discount and state that it is available only when the invoice is paid in full on or before its due date. The offer is not deducted from the gross invoice total at issue time.

After payment allocation confirms that the invoice has been fully settled, the application evaluates the full-payment receipt date against the invoice due date (inclusive). If the full payment date is on or before the due date, the offered amount is recorded as the applied discount; otherwise the applied amount remains zero. Partial payments do not qualify, even if made before the due date. A missing due date or missing full-payment date also does not qualify.

Persist the offer in `invoices.early_payment_discount_amount`, the amount earned in `invoices.early_payment_discount_applied_amount`, and the application timestamp in `invoices.early_payment_discount_applied_at`. Database constraints prevent negative amounts or an applied amount greater than the offer. The invoice's original gross `total_amount` remains unchanged; the applied discount is a separate settlement adjustment. The payment-allocation module must be the only component that marks the discount as earned.

The eligibility policy is covered by unit tests for full payment before/on/after the due date, partial payment, missing dates, zero offers and negative offers. The current Phase 1 codebase does not yet contain payment allocation or PDF generation, so those integrations remain required before this behavior can be exercised end-to-end or displayed in a generated PDF.

## 4. API contract

### 4.1 API conventions

- Base path: `/api/v1`.
- JSON request/response bodies; bean validation on all externally supplied fields.
- Paginate collection endpoints and use a consistent error envelope, for example: `code`, `message`, `fieldErrors`, `traceId`.
- Require an `Idempotency-Key` for create/issue/payment endpoints where retries could create duplicate effects.
- Publish the OpenAPI specification at `/docs`.
- Cross-tenant resource IDs return 404. Unauthenticated requests return 401; authenticated users lacking permission receive 403; invalid input/business rules return 400 or 422 according to the agreed API convention.

### 4.2 Initial endpoint proposal

| Method | Path | Purpose |
|---|---|---|
| POST | `/auth/login` | Authenticate user |
| POST | `/auth/refresh` | Rotate refresh token |
| GET | `/organisations/current` | Return current authenticated tenant context |
| GET, POST | `/customers` | List/create customers |
| GET, PUT | `/customers/{customerId}` | Read/update tenant-scoped customer |
| GET, POST | `/invoices` | List/create draft invoices |
| GET, PUT | `/invoices/{invoiceId}` | Read/update draft invoice |
| POST | `/invoices/{invoiceId}/issue` | Validate, calculate tax, allocate number and issue |
| POST | `/invoices/{invoiceId}/credit-notes` | Create linked credit note |
| GET | `/invoices/{invoiceId}/document` | Return signed PDF download URL |
| GET, POST | `/recurring-templates` | List/create recurring invoice templates |
| POST | `/recurring-templates/{templateId}/activate` | Activate recurring template |
| GET | `/audit-events` | List tenant-scoped audit events |

Endpoint permissions must match the matrix in Section 1. Do not expose update/delete operations for issued invoice content.

## 5. Tax rule

### 5.1 Calculation rule

FR-1.04 requires GST to be calculated **per invoice line** using the tax rate and place of supply:

- **Intra-state supply:** split the applicable GST rate equally into CGST and SGST.
- **Inter-state supply:** apply the applicable GST rate as IGST.
- The requirements do not specify tax rates, exemptions, the authoritative place-of-supply determination, or the rounding mode. Those configuration/legal assumptions require mentor confirmation.

Proposed deterministic rounding rule: use Java `BigDecimal`; round **each line's tax component independently to two decimal places using `RoundingMode.HALF_UP`**; then sum the rounded line components. Do not calculate tax only once on the invoice subtotal.

### 5.2 Hand calculation procedure

For each line:

1. Calculate taxable base: `quantity × unit price − line discount`.
2. If supplier state equals place-of-supply state:
   - `CGST = round(taxable base × GST rate ÷ 2, 2, HALF_UP)`
   - `SGST = round(taxable base × GST rate ÷ 2, 2, HALF_UP)`
3. Otherwise:
   - `IGST = round(taxable base × GST rate, 2, HALF_UP)`
4. Line total = taxable base + the rounded tax component(s).
5. Invoice subtotal = sum of line taxable bases; invoice tax total = sum of rounded line tax components; invoice total = sum of line totals.

**Worked example (illustrative 18% GST):**

| Case | Taxable base | Calculation | Rounded tax |
|---|---:|---|---:|
| Intra-state CGST | 100.00 | 100.00 × 9% | 9.00 |
| Intra-state SGST | 100.00 | 100.00 × 9% | 9.00 |
| Inter-state IGST | 100.00 | 100.00 × 18% | 18.00 |

For a ₹100.00 taxable line, intra-state tax is ₹9.00 CGST + ₹9.00 SGST = ₹18.00; line total is ₹118.00. For an inter-state line, tax is ₹18.00 IGST; line total is also ₹118.00.

Rounding example: taxable base ₹0.05 at 5% gives raw tax ₹0.0025. Under the proposed HALF_UP rule, that line's tax rounds to ₹0.00 at two decimal places. Apply the same process separately to each line before summing.

Persist the applied tax rate, place of supply, tax type/split and calculated amounts on issued invoice lines so historical invoices remain unchanged if configuration changes. Verify same-state, other-state and mixed-rate invoices, awkward decimal values and discounts. AC-1.3 requires reviewer hand-checking of three tax cases; D-1.6 requires sample invoice PDFs.

## 6. Numbering rule

### 6.1 Series and format

FR-1.05 requires a configurable invoice-number series per financial year, with no gaps or duplicates even when invoices are issued concurrently.

Illustrative format: `INV/2026-27/000001`. Prefix, financial-year label and zero-padding are configurable per organisation/series; this example is not mandated by the requirements. The sequence scope is organisation + financial year + series.

### 6.2 Hand-applicable allocation procedure

1. A draft invoice has no final invoice number and does not consume a sequence value.
2. When the user issues the invoice, begin a database transaction.
3. Lock the matching `invoice_series` row for writing (for PostgreSQL, use `SELECT ... FOR UPDATE`).
4. Read `next_number`. If it is 41, assign the formatted number ending in `000041` to this invoice.
5. Increment `next_number` to 42 in the same transaction.
6. Persist the invoice as issued and persist its audit event in the same transaction; then commit.
7. If any operation fails before commit, roll back the transaction. Both the invoice issue and counter increment roll back, so the next successful issue can use 41.
8. Enforce a unique database constraint over organisation, financial year, series and invoice number. Require an idempotency key on issue requests so a retried request does not issue a second invoice.
9. Never renumber or reuse an issued number. A voided invoice keeps its number and audit history. Corrections use a linked credit note.

The sequence must not be generated in application memory or Redis. Allocate it in the database transaction that issues the invoice.

**Concurrency acceptance test:** issue 20 invoices in parallel for the same organisation, financial year and series. The committed invoices must receive 20 unique consecutive numbers, with no gaps or duplicates (AC-1.2). Also test rollback and duplicate idempotency-key behavior. The no-gap guarantee concerns successfully issued invoices; failed transactions must not consume a number.

## 7. Risks and open decisions

| Risk / open decision | Mitigation / action |
|---|---|
| Team-specific client rule (FR-1.08) is missing from the PDF | Retrieve it from “Your client” in Team Workspace; amend data model/core flow and add an automated test before design sign-off. |
| Rounding differences between line and invoice totals | Agree the rounding mode and currency precision with mentor; implement one shared calculation service and test awkward amounts. |
| Invoice-number gaps or duplicates under concurrency | Lock the series row and increment inside the issuing transaction; add a unique constraint and 20-parallel-issue test. |
| Permission assumptions do not match business expectations | Obtain mentor approval of Section 1 and add permitted/denied tests for every protected endpoint. |
| GST rates/place-of-supply inputs are not fully specified | Confirm rates, supplier state, GSTIN/state validation and place-of-supply source before implementation. |
| Recurring job runs twice or misses a scheduled date | Use an idempotent occurrence key (template ID + scheduled period), persisted run state, retry/backoff and failure visibility. Confirm timezone and behavior for days 29/30/31. |
| PDF generation or object storage fails after invoice issue | Persist document-generation state; retry asynchronous generation; expose status and do not mutate the issued financial snapshot. |
| Concurrent edits to a draft overwrite one another | Use optimistic locking/version checks and return a conflict response when the submitted version is stale. |

### Mentor decisions requested

1. Approve or amend the role-by-action matrix.
2. Confirm the tax rounding mode, currency precision, tax-rate source and place-of-supply rule.
3. Approve the configurable series format and transactional allocation behavior.
4. Confirm CR-1 scheduling details: timezone, month-end dates, retries and automatic issue semantics.
5. Supply/confirm the team-specific client rule for FR-1.08.
6. Confirm any Phase 1 payment-related permission that differs from the proposed matrix.

## Source traceability

Primary source: **LogicVeda Technologies, PayTrack Phase 1: Invoicing Core — Project Requirements Document**, LV-JAVA-PAYTRACK-P1, v1.0, issued 12 November 2026. This design is based on FR-1.01–FR-1.08, CR-1, NFR-01–NFR-11, AC-1.1–AC-1.5, the technology standard in Section 7, engineering rules and deliverables in Sections 8–9.

The source mandates outcomes such as tenant isolation, a role/action permission matrix, per-line GST, documented rounding and concurrent gap-free numbering. It does **not** prescribe the detailed role grants, HALF_UP mode, example invoice-number format, full schema or exact endpoint paths; those are proposals in this draft for mentor review.
