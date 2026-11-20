package com.paytrack.document.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.invoicing.infrastructure.persistence.entity.Invoice;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.Instant;

@Entity @Table(name="invoice_documents", uniqueConstraints=@UniqueConstraint(name="uk_invoice_document_version", columnNames={"invoice_id","document_version"}))
@Getter @Setter @NoArgsConstructor
public class InvoiceDocument extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="invoice_id", nullable=false) private Invoice invoice;
 @Column(name="file_name", nullable=false, length=255) private String fileName;
 @Column(name="storage_key", nullable=false, length=500) private String storageKey;
 @Column(name="content_type", nullable=false, length=120) private String contentType;
 @Column(name="file_size", nullable=false) private Long fileSize;
 @Column(name="document_version", nullable=false) private Integer documentVersion;
 @Column(name="generated_at", nullable=false) private Instant generatedAt;
}