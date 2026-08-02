package com.hst.materialmgmt.entity;

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
@Table(value = "sales_route_tbl", schema = "erp_finance_schema")
public class SalesRouteEntity extends BaseEntity {

    @Id @Column("route_id")    private String routeId;
    @Column("route_name")      private String routeName;
    @Column("area_name")       private String areaName;
    @Column("is_active")       private Boolean isActive;

    @Override public String getId() { return routeId; }
}
