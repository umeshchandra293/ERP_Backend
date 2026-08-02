package com.hst.materialmgmt.rowMapper;

import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesRouteEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesRouteRowMapper extends BaseRowMapper<SalesRouteEntity> {
    @Override
    public SalesRouteEntity apply(Row row, RowMetadata meta) {
        SalesRouteEntity e = SalesRouteEntity.builder()
                .routeId(row.get("route_id",   String.class))
                .routeName(row.get("route_name", String.class))
                .areaName(row.get("area_name",  String.class))
                .isActive(row.get("is_active",  Boolean.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
