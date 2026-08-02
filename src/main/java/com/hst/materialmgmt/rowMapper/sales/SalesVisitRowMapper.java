package com.hst.materialmgmt.rowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesVisitEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesVisitRowMapper extends BaseRowMapper<SalesVisitEntity> {
    @Override
    public SalesVisitEntity apply(Row row, RowMetadata meta) {
        SalesVisitEntity e = SalesVisitEntity.builder()
                .visitId(row.get("visit_id", String.class))
                .salesmanId(row.get("salesman_id", String.class))
                .retailerId(row.get("retailer_id", String.class))
                .visitDate(row.get("visit_date", LocalDate.class))
                .checkInTime(row.get("check_in_time", LocalDateTime.class))
                .gpsLat(row.get("gps_lat", BigDecimal.class))
                .gpsLng(row.get("gps_lng", BigDecimal.class))
                .gpsVerified(row.get("gps_verified", Boolean.class))
                .remarks(row.get("remarks", String.class))
                .status(row.get("status", String.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
