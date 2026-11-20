package com.paytrack.recurring.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.customer.infrastructure.persistence.entity.Customer;
import com.paytrack.identity.infrastructure.persistence.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.LocalDate;

@Entity @Table(name="recurring_invoice_templates")
@Getter @Setter @NoArgsConstructor
public class RecurringInvoiceTemplate extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="customer_id", nullable=false) private Customer customer;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="created_by", nullable=false) private AppUser createdBy;
 @Column(name="template_name", nullable=false, length=160) private String templateName;
 @Column(nullable=false, length=30) private String frequency;
 @Column(name="issue_day", nullable=false) private Integer issueDay;
 @Column(name="next_issue_date", nullable=false) private LocalDate nextIssueDate;
 @Column(name="start_date", nullable=false) private LocalDate startDate;
 @Column(name="end_date") private LocalDate endDate;
 @Column(nullable=false, length=30) private String status;
}