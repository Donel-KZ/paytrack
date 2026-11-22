package com.paytrack.identity.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name="refresh_tokens", uniqueConstraints=@UniqueConstraint(name="uk_refresh_token_hash", columnNames="token_hash"))
@Getter @Setter @NoArgsConstructor
public class RefreshToken extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false) private AppUser user;
 @Column(name="token_hash", nullable=false, length=255) private String tokenHash;
 @Column(name="expires_at", nullable=false) private Instant expiresAt;
 @Column(name="revoked_at") private Instant revokedAt;
}