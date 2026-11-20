package com.paytrack.shared.persistence;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.identity.infrastructure.persistence.entity.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity @Table(name="idempotency_records", uniqueConstraints=@UniqueConstraint(name="uk_idempotency_org_key", columnNames={"organisation_id","idempotency_key"}))
@Getter @Setter @NoArgsConstructor
public class IdempotencyRecord extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false) private AppUser user;
 @Column(name="idempotency_key", nullable=false, length=150) private String idempotencyKey;
 @Column(name="request_hash", nullable=false, length=128) private String requestHash;
 @Column(name="response_status") private Integer responseStatus;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="response_body", columnDefinition="jsonb") private JsonNode responseBody;
 @Column(name="expires_at") private Instant expiresAt;
}