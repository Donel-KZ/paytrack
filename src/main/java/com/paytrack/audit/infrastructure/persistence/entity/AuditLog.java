package com.paytrack.audit.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.identity.infrastructure.persistence.entity.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity @Table(name="audit_logs")
@Getter @Setter @NoArgsConstructor
public class AuditLog extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private AppUser user;
 @Column(nullable=false, length=100) private String action;
 @Column(name="entity_type", nullable=false, length=100) private String entityType;
 @Column(name="entity_id", nullable=false) private UUID entityId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="old_values", columnDefinition="jsonb") private JsonNode oldValues;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="new_values", columnDefinition="jsonb") private JsonNode newValues;
 @Column(name="ip_address", length=64) private String ipAddress;
}