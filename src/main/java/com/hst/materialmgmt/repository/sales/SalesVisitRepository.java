package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesVisitEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesVisitRowMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SalesVisitRepository extends ParentRepositoryImpl {

    @Autowired private SalesVisitRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_visit_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) {
        return Map.of("visit_id", id);
    }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesVisitEntity.class; }

    public Flux<SalesVisitEntity> findFiltered(
            String salesmanId, String retailerId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM erp_finance_schema.sales_visit_tbl WHERE 1=1");
        if (salesmanId != null) sql.append(" AND salesman_id = :salesmanId");
        if (retailerId != null) sql.append(" AND retailer_id = :retailerId");
        if (from != null)       sql.append(" AND visit_date >= :fromDate");
        if (to != null)         sql.append(" AND visit_date <= :toDate");
        sql.append(" ORDER BY visit_date DESC, check_in_time DESC");

        var spec = databaseClient.sql(sql.toString());
        if (salesmanId != null) spec = spec.bind("salesmanId", salesmanId);
        if (retailerId != null) spec = spec.bind("retailerId", retailerId);
        if (from != null)       spec = spec.bind("fromDate", from);
        if (to != null)         spec = spec.bind("toDate", to);

        return spec.map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<Long> countTodayBySalesman(String salesmanId) {
        String sql = "SELECT COUNT(*) FROM erp_finance_schema.sales_visit_tbl " +
                     "WHERE salesman_id = :salesmanId AND visit_date = CURRENT_DATE";
        return databaseClient.sql(sql)
                .bind("salesmanId", salesmanId)
                .map((row, meta) -> row.get(0, Long.class)).one();
    }

    /** nextval lives here so service doesn't need direct databaseClient access */
    public Mono<String> nextVisitId() {
        return databaseClient
                .sql("SELECT nextval('erp_finance_schema.sales_visit_seq')")
                .map((row, meta) -> row.get(0, Long.class)).one()
                .map(n -> String.format("VIS-%06d", n));
    }
}
