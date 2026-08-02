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
@Table(value = "sales_product_tbl", schema = "erp_finance_schema")
public class SalesProductEntity extends BaseEntity {

    @Id @Column("product_id")   private String productId;
    @Column("product_name")     private String productName;
    @Column("sku")              private String sku;
    @Column("base_price")       private BigDecimal basePrice;
    @Column("unit")             private String unit;
    @Column("is_active")        private Boolean isActive;

    @Override public String getId() { return productId; }
}
