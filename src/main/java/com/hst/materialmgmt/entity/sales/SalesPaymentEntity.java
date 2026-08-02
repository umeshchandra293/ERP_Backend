package com.hst.materialmgmt.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data @EqualsAndHashCode(callSuper = true)
@AllArgsConstructor @NoArgsConstructor
@Accessors(chain = true) @SuperBuilder
@Table(value = "sales_payment_tbl", schema = "erp_finance_schema")
public class SalesPaymentEntity extends BaseEntity {

    @Id @Column("payment_id")       private String paymentId;
    @Column("salesman_id")          private String salesmanId;
    @Column("retailer_id")          private String retailerId;
    @Column("visit_id")             private String visitId;
    @Column("payment_date")         private LocalDate paymentDate;
    @Column("amount")               private BigDecimal amount;
    @Column("payment_mode")         private String paymentMode;
    @Column("reference_number")     private String referenceNumber;
    @Column("notes")                private String notes;

    @Override public String getId() { return paymentId; }
}
