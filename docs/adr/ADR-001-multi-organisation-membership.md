# ADR-001: Users may belong to multiple organisations

- **Status:** Accepted
- **Date:** 16 November 2026
- **Decision owners:** PayTrack backend team
- **Context:** PayTrack is a multi-tenant invoicing platform. A person may work for more than one distributor and must be able to switch between those organisations without maintaining separate login accounts.

## Decision

A user account may have memberships in multiple organisations. The user's role is assigned **per organisation membership**, not globally on the user account.

The relationship is modelled as:

```text
users
  1 ─── many organisation_memberships many ─── 1 organisations

organisation_memberships
  - id
  - user_id
  - organisation_id
  - role
  - status
  - created_at
  - updated_at

UNIQUE (user_id, organisation_id)
```

A membership represents the user's access to one organisation. The same user may be an `OWNER` in one organisation and an `ACCOUNTANT` or `READ_ONLY` member in another, subject to the approved role-management rules.

## Request and tenant-selection behaviour

1. The user authenticates once and receives an authenticated identity.
2. The backend exposes the organisations for which the user has active memberships.
3. The user selects an organisation in the client.
4. The client identifies the selected organisation on subsequent requests using the agreed tenant-selection mechanism (for example, an organisation context header or a tenant-scoped route).
5. The backend independently verifies that the authenticated user has an active membership in that organisation.
6. The backend loads the role from that membership and builds the trusted tenant context for the request.
7. Every tenant-owned query and mutation is scoped to that verified organisation ID.

The organisation identifier supplied by the client is a **selection hint only**. It is never proof of authorisation. If the user has no active membership in the requested organisation, the request is denied without revealing another tenant's data.

## Security and integrity rules

- Never store one global role on `users`; store role on `organisation_memberships`.
- Enforce `UNIQUE(user_id, organisation_id)` to prevent duplicate memberships.
- Membership status must be checked on each authenticated request or through a revocation-aware cache.
- Deactivating a membership revokes access to that organisation without disabling the user's memberships in other organisations.
- Role changes in one organisation must not affect the user's role in another.
- Every tenant-owned record still carries `organisation_id`.
- Cross-tenant resource lookups return 404 as required by NFR-01.
- Audit membership creation, deactivation and role changes, recording the actor and affected organisation.
- Token claims may identify the user, but the backend must not rely on a stale global role claim. If organisation and role claims are included in a short-lived tenant-scoped token, membership status and revocation behaviour must still be defined and tested.

## Consequences

### Positive

- One identity can securely work across multiple distributor tenants.
- Roles are independently configurable for each organisation.
- Removing a user's access to one distributor does not affect other memberships.
- The model supports future organisation switching in the frontend.

### Trade-offs

- Each request must establish and verify the active organisation context.
- Repository access patterns and tests must consistently include the organisation ID.
- Authentication and tenant authorisation are separate concerns: a valid login does not imply access to every organisation.

## Required tests

1. A user with active memberships in organisations A and B can access resources in either organisation after selecting the corresponding context.
2. The user's role in A is applied only in A; the role in B is applied only in B.
3. A user who is not a member of organisation C cannot select C, even if they know its identifier.
4. Deactivating the membership in A blocks access to A but preserves access to B.
5. A user from A cannot retrieve an invoice belonging to B by guessing its invoice ID; the API returns 404.
6. Duplicate membership creation for the same user and organisation is rejected by the database constraint.

## Follow-up implementation

- Create `organisation_memberships` in the initial Flyway schema.
- Add an endpoint to list the current user's available organisations and memberships.
- Implement a tenant-context resolver that verifies membership before controller/application processing.
- Add integration tests using PostgreSQL Testcontainers.
