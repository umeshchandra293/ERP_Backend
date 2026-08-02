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
@Table(value = "sales_retailer_tbl", schema = "erp_finance_schema")
public class RetailerEntity extends BaseEntity {

    @Id @Column("retailer_id")   private String retailerId;
    @Column("shop_name")         private String shopName;
    @Column("owner_name")        private String ownerName;
    @Column("phone")             private String phone;
    @Column("address")           private String address;
    @Column("area")              private String area;
    @Column("gps_lat")           private BigDecimal gpsLat;
    @Column("gps_lng")           private BigDecimal gpsLng;
    @Column("assigned_salesman_id") private String assignedSalesmanId;
    @Column("credit_limit")      private BigDecimal creditLimit;
    @Column("current_balance")   private BigDecimal currentBalance;
    @Column("is_active")         private Boolean isActive;

    @Override public String getId() { return retailerId; }
}
