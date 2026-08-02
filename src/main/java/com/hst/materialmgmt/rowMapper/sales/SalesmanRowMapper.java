package com.hst.materialmgmt.rowMapper;

import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesmanEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesmanRowMapper extends BaseRowMapper<SalesmanEntity> {
    @Override
    public SalesmanEntity apply(Row row, RowMetadata meta) {
        SalesmanEntity e = SalesmanEntity.builder()
                .salesmanId(row.get("salesman_id", String.class))
                .username(row.get("username", String.class))
                .fullName(row.get("full_name", String.class))
                .phone(row.get("phone", String.class))
                .routeId(row.get("route_id", String.class))
                .isActive(row.get("is_active", Boolean.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
