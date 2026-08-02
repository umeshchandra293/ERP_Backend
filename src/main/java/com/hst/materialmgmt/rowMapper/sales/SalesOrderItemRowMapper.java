package com.hst.materialmgmt.rowMapper;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesOrderItemEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesOrderItemRowMapper extends BaseRowMapper<SalesOrderItemEntity> {
    @Override
    public SalesOrderItemEntity apply(Row row, RowMetadata meta) {
        SalesOrderItemEntity e = SalesOrderItemEntity.builder()
                .itemId(row.get("item_id", String.class))
                .orderId(row.get("order_id", String.class))
                .productId(row.get("product_id", String.class))
                .quantity(row.get("quantity", BigDecimal.class))
                .unitPrice(row.get("unit_price", BigDecimal.class))
                .lineTotal(row.get("line_total", BigDecimal.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
