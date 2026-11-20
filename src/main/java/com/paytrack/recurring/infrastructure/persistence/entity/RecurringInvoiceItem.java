package com.paytrack.recurring.infrastructure.persistence.entity;
import com.paytrack.shared.persistence.BaseEntity;
import com.paytrack.organisation.infrastructure.persistence.entity.Organisation;
import com.paytrack.catalogue.infrastructure.persistence.entity.Product;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="recurring_invoice_items")
@Getter @Setter @NoArgsConstructor
public class RecurringInvoiceItem extends BaseEntity {
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="organisation_id", nullable=false) private Organisation organisation;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="template_id", nullable=false) private RecurringInvoiceTemplate template;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
 @Column(nullable=false, length=500) private String description;
 @Column(nullable=false, precision=19, scale=4) private BigDecimal quantity;
 @Column(name="unit_price", nullable=false, precision=19, scale=4) private BigDecimal unitPrice;
 @Column(name="tax_rate", nullable=false, precision=7, scale=4) private BigDecimal taxRate;
}