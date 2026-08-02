package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesPaymentEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesPaymentRowMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SalesPaymentRepository extends ParentRepositoryImpl {

    @Autowired private SalesPaymentRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_payment_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("payment_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesPaymentEntity.class; }

    public Flux<SalesPaymentEntity> findFiltered(
            String salesmanId, String retailerId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM erp_finance_schema.sales_payment_tbl WHERE 1=1");
        if (salesmanId != null) sql.append(" AND salesman_id = :salesmanId");
        if (retailerId != null) sql.append(" AND retailer_id = :retailerId");
        if (from != null)       sql.append(" AND payment_date >= :fromDate");
        if (to != null)         sql.append(" AND payment_date <= :toDate");
        sql.append(" ORDER BY payment_date DESC");

        var spec = databaseClient.sql(sql.toString());
        if (salesmanId != null) spec = spec.bind("salesmanId", salesmanId);
        if (retailerId != null) spec = spec.bind("retailerId", retailerId);
        if (from != null)       spec = spec.bind("fromDate", from);
        if (to != null)         spec = spec.bind("toDate", to);

        return spec.map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<BigDecimal> sumTodayBySalesman(String salesmanId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM erp_finance_schema.sales_payment_tbl " +
                     "WHERE salesman_id = :salesmanId AND payment_date = CURRENT_DATE";
        return databaseClient.sql(sql)
                .bind("salesmanId", salesmanId)
                .map((row, meta) -> row.get(0, BigDecimal.class)).one();
    }
}
