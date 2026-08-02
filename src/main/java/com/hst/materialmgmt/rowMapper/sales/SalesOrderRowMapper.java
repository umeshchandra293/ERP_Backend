package com.hst.materialmgmt.rowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesOrderEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesOrderRowMapper extends BaseRowMapper<SalesOrderEntity> {
    @Override
    public SalesOrderEntity apply(Row row, RowMetadata meta) {
        SalesOrderEntity e = SalesOrderEntity.builder()
                .orderId(row.get("order_id", String.class))
                .salesmanId(row.get("salesman_id", String.class))
                .retailerId(row.get("retailer_id", String.class))
                .visitId(row.get("visit_id", String.class))
                .orderDate(row.get("order_date", LocalDate.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .status(row.get("status", String.class))
                .notes(row.get("notes", String.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
