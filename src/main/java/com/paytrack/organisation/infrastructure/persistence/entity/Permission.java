package com.paytrack.organisation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "permissions", uniqueConstraints = {@UniqueConstraint(name = "uk_permissions_name", columnNames = "name")})
@Getter
@Setter
@NoArgsConstructor
public class Permission extends BaseEntity {
    @Column(nullable = false, length = 100) private String name;

    @Column(length = 255) private String description;
}
