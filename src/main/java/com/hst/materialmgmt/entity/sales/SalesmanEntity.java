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
@Table(value = "sales_salesman_tbl", schema = "erp_finance_schema")
public class SalesmanEntity extends BaseEntity {

    @Id @Column("salesman_id")  private String salesmanId;
    @Column("username")         private String username;
    @Column("full_name")        private String fullName;
    @Column("phone")            private String phone;
    @Column("route_id")         private String routeId;
    @Column("is_active")        private Boolean isActive;

    @Override public String getId() { return salesmanId; }
}
