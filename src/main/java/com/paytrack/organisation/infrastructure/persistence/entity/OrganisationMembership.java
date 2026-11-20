package com.paytrack.organisation.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.paytrack.shared.persistence.BaseEntity;

@Entity
@Table(name = "organisation_memberships", uniqueConstraints = {@UniqueConstraint(name = "uk_membership_user_organisation", columnNames = {"user_id", "organisation_id"})})
@Getter
@Setter
@NoArgsConstructor
public class OrganisationMembership extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private com.paytrack.identity.infrastructure.persistence.entity.AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "role_id", nullable = false) private Role role;

    @Column(nullable = false, length = 30) private String status;

    @Column(name = "joined_at") private java.time.Instant joinedAt;
}
