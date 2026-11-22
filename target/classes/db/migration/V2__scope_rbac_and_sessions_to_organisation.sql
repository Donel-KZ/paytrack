-- Add explicit tenant ownership to RBAC, role-permission mappings and refresh sessions.
-- organisations is the tenant root; users is a global identity so one person can
-- hold memberships in multiple organisations. Those two tables intentionally do not
-- carry organisation_id.

ALTER TABLE roles ADD COLUMN organisation_id UUID NOT NULL;
ALTER TABLE permissions ADD COLUMN organisation_id UUID NOT NULL;
ALTER TABLE role_permissions ADD COLUMN organisation_id UUID NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN organisation_id UUID NOT NULL;

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_organisation
    FOREIGN KEY (organisation_id) REFERENCES organisations(id);
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_organisation
    FOREIGN KEY (organisation_id) REFERENCES organisations(id);
ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_organisation
    FOREIGN KEY (organisation_id) REFERENCES organisations(id);

ALTER TABLE roles
    ADD CONSTRAINT uq_roles_org_id UNIQUE (organisation_id, id);
ALTER TABLE permissions
    ADD CONSTRAINT uq_permissions_org_id UNIQUE (organisation_id, id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_role_tenant
    FOREIGN KEY (organisation_id, role_id) REFERENCES roles(organisation_id, id);
ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_permission_tenant
    FOREIGN KEY (organisation_id, permission_id) REFERENCES permissions(organisation_id, id);

ALTER TABLE roles DROP CONSTRAINT uk_roles_name;
ALTER TABLE roles ADD CONSTRAINT uk_roles_org_name UNIQUE (organisation_id, name);
ALTER TABLE permissions DROP CONSTRAINT uk_permissions_name;
ALTER TABLE permissions ADD CONSTRAINT uk_permissions_org_name UNIQUE (organisation_id, name);
ALTER TABLE role_permissions DROP CONSTRAINT uk_role_permission;
ALTER TABLE role_permissions ADD CONSTRAINT uk_role_permission_org UNIQUE (organisation_id, role_id, permission_id);

CREATE INDEX ix_roles_organisation ON roles(organisation_id);
CREATE INDEX ix_permissions_organisation ON permissions(organisation_id);
CREATE INDEX ix_role_permissions_organisation ON role_permissions(organisation_id);
CREATE INDEX ix_refresh_tokens_org_user_expiry ON refresh_tokens(organisation_id, user_id, expires_at);
