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
@Table(value = "sales_order_tbl", schema = "erp_finance_schema")
public class SalesOrderEntity extends BaseEntity {

    @Id @Column("order_id")    private String orderId;
    @Column("salesman_id")     private String salesmanId;
    @Column("retailer_id")     private String retailerId;
    @Column("visit_id")        private String visitId;
    @Column("order_date")      private LocalDate orderDate;
    @Column("total_amount")    private BigDecimal totalAmount;
    @Column("status")          private String status;
    @Column("notes")           private String notes;

    @Override public String getId() { return orderId; }
}
