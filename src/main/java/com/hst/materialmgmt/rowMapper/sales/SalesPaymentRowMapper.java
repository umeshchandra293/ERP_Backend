package com.hst.materialmgmt.rowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import com.hst.materialmgmt.entity.SalesPaymentEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
public class SalesPaymentRowMapper extends BaseRowMapper<SalesPaymentEntity> {
    @Override
    public SalesPaymentEntity apply(Row row, RowMetadata meta) {
        SalesPaymentEntity e = SalesPaymentEntity.builder()
                .paymentId(row.get("payment_id", String.class))
                .salesmanId(row.get("salesman_id", String.class))
                .retailerId(row.get("retailer_id", String.class))
                .visitId(row.get("visit_id", String.class))
                .paymentDate(row.get("payment_date", LocalDate.class))
                .amount(row.get("amount", BigDecimal.class))
                .paymentMode(row.get("payment_mode", String.class))
                .referenceNumber(row.get("reference_number", String.class))
                .notes(row.get("notes", String.class))
                .build();
        populateAuditInfo(e, row);
        return e;
    }
}
