package com.paytrack.organisation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {@UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})})
@Getter
@Setter
@NoArgsConstructor
public class RolePermission extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "role_id", nullable = false) private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "permission_id", nullable = false) private Permission permission;
}
