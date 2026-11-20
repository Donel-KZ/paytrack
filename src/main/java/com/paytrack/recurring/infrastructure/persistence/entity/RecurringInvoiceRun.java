package com.paytrack.recurring.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.invoicing.infrastructure.persistence.entity.Invoice;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.Instant; import java.time.LocalDate;

@Entity @Table(name="recurring_invoice_runs", uniqueConstraints=@UniqueConstraint(name="uk_recurring_run_template_date", columnNames={"template_id","scheduled_date"}))
@Getter @Setter @NoArgsConstructor
public class RecurringInvoiceRun extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="template_id", nullable=false) private RecurringInvoiceTemplate template;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invoice_id") private Invoice invoice;
 @Column(name="scheduled_date", nullable=false) private LocalDate scheduledDate;
 @Column(name="execution_started_at") private Instant executionStartedAt;
 @Column(name="execution_completed_at") private Instant executionCompletedAt;
 @Column(nullable=false, length=30) private String status;
 @Column(name="attempt_count", nullable=false) private Integer attemptCount=0;
 @Column(name="error_message", length=2000) private String errorMessage;
}