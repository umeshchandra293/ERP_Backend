package com.hst.materialmgmt.rowMapper;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesProductEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesProductRowMapper extends BaseRowMapper<SalesProductEntity> {
    @Override
    public SalesProductEntity apply(Row row, RowMetadata meta) {
        SalesProductEntity e = SalesProductEntity.builder()
                .productId(row.get("product_id", String.class))
                .productName(row.get("product_name", String.class))
                .sku(row.get("sku", String.class))
                .basePrice(row.get("base_price", BigDecimal.class))
                .unit(row.get("unit", String.class))
                .isActive(row.get("is_active", Boolean.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
