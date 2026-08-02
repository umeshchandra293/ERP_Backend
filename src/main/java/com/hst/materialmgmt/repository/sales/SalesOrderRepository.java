package com.hst.materialmgmt.repository;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesOrderEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesOrderRowMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SalesOrderRepository extends ParentRepositoryImpl {

    @Autowired private SalesOrderRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_order_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("order_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesOrderEntity.class; }

    public Flux<SalesOrderEntity> findFiltered(
            String salesmanId, String retailerId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM erp_finance_schema.sales_order_tbl WHERE 1=1");
        if (salesmanId != null) sql.append(" AND salesman_id = :salesmanId");
        if (retailerId != null) sql.append(" AND retailer_id = :retailerId");
        if (from != null)       sql.append(" AND order_date >= :fromDate");
        if (to != null)         sql.append(" AND order_date <= :toDate");
        sql.append(" ORDER BY order_date DESC");

        var spec = databaseClient.sql(sql.toString());
        if (salesmanId != null) spec = spec.bind("salesmanId", salesmanId);
        if (retailerId != null) spec = spec.bind("retailerId", retailerId);
        if (from != null)       spec = spec.bind("fromDate", from);
        if (to != null)         spec = spec.bind("toDate", to);

        return spec.map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<Long> countTodayBySalesman(String salesmanId) {
        String sql = "SELECT COUNT(*) FROM erp_finance_schema.sales_order_tbl " +
                     "WHERE salesman_id = :salesmanId AND order_date = CURRENT_DATE";
        return databaseClient.sql(sql)
                .bind("salesmanId", salesmanId)
                .map((row, meta) -> row.get(0, Long.class)).one();
    }

    public Mono<Void> updateStatus(String orderId, String status) {
        String sql = "UPDATE erp_finance_schema.sales_order_tbl " +
                     "SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE order_id = :id";
        return databaseClient.sql(sql)
                .bind("status", status).bind("id", orderId)
                .fetch().rowsUpdated().then();
    }
}
