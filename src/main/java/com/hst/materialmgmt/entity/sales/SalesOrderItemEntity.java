package com.hst.materialmgmt.entity;

import java.math.BigDecimal;

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
@Table(value = "sales_order_item_tbl", schema = "erp_finance_schema")
public class SalesOrderItemEntity extends BaseEntity {

    @Id @Column("item_id")     private String itemId;
    @Column("order_id")        private String orderId;
    @Column("product_id")      private String productId;
    @Column("quantity")        private BigDecimal quantity;
    @Column("unit_price")      private BigDecimal unitPrice;
    @Column("line_total")      private BigDecimal lineTotal;

    @Override public String getId() { return itemId; }
}
