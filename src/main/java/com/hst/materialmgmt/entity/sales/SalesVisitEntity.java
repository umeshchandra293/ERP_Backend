package com.hst.materialmgmt.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(value = "sales_visit_tbl", schema = "erp_finance_schema")
public class SalesVisitEntity extends BaseEntity {

    @Id @Column("visit_id")      private String visitId;
    @Column("salesman_id")       private String salesmanId;
    @Column("retailer_id")       private String retailerId;
    @Column("visit_date")        private LocalDate visitDate;
    @Column("check_in_time")     private LocalDateTime checkInTime;
    @Column("gps_lat")           private BigDecimal gpsLat;
    @Column("gps_lng")           private BigDecimal gpsLng;
    @Column("gps_verified")      private Boolean gpsVerified;
    @Column("remarks")           private String remarks;
    @Column("status")            private String status;

    @Override public String getId() { return visitId; }
}
