package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesmanEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesmanRowMapper;
import com.hst.materialmgmt.service.SalesmanActivityRowData;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SalesmanRepository extends ParentRepositoryImpl {

    @Autowired private SalesmanRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_salesman_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) {
        return Map.of("salesman_id", id);
    }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesmanEntity.class; }

    public Mono<SalesmanEntity> findByUsername(String username) {
        String sql = "SELECT * FROM erp_finance_schema.sales_salesman_tbl WHERE username = :username";
        return databaseClient.sql(sql)
                .bind("username", username)
                .map((row, meta) -> rowMapper.apply(row, meta)).one();
    }

    /** Next salesman ID from sequence — lives here so service doesn't touch databaseClient */
    public Mono<String> nextSalesmanId() {
        return databaseClient
                .sql("SELECT nextval('erp_finance_schema.sales_salesman_seq')")
                .map((row, meta) -> row.get(0, Long.class)).one()
                .map(n -> String.format("SMAN-%03d", n));
    }

    /** Today's targets for a salesman — returns {visitTarget, orderTarget} or {0,0} */
    public Mono<long[]> findTodayTarget(String salesmanId) {
        String sql = """
            SELECT
                COALESCE(visit_target, 0) AS vt,
                COALESCE(order_target, 0) AS ot
            FROM erp_finance_schema.sales_daily_target_tbl
            WHERE salesman_id = :salesmanId
              AND target_date = CURRENT_DATE
            """;
        return databaseClient.sql(sql)
                .bind("salesmanId", salesmanId)
                .map((row, meta) -> {
                    Long vt = row.get("vt", Long.class);
                    Long ot = row.get("ot", Long.class);
                    return new long[]{ vt != null ? vt : 0L, ot != null ? ot : 0L };
                })
                .one()
                .defaultIfEmpty(new long[]{0L, 0L});
    }

    /** Activity aggregation for admin dashboard */
    public Flux<SalesmanActivityRowData> findActivityForDate(LocalDate date) {
        String sql = """
            SELECT
                s.salesman_id,
                s.full_name                   AS salesman_name,
                COALESCE(r.route_name, 'N/A') AS route_name,
                COALESCE(tgt.visit_target, 0) AS visit_target,
                COUNT(DISTINCT v.visit_id)    AS visits_today,
                COUNT(DISTINCT o.order_id)    AS orders_today,
                COALESCE(SUM(p.amount), 0)    AS collections_today
            FROM erp_finance_schema.sales_salesman_tbl s
            LEFT JOIN erp_finance_schema.sales_route_tbl r
                   ON r.route_id = s.route_id
            LEFT JOIN erp_finance_schema.sales_daily_target_tbl tgt
                   ON tgt.salesman_id = s.salesman_id AND tgt.target_date = :date
            LEFT JOIN erp_finance_schema.sales_visit_tbl v
                   ON v.salesman_id = s.salesman_id AND v.visit_date = :date
            LEFT JOIN erp_finance_schema.sales_order_tbl o
                   ON o.salesman_id = s.salesman_id AND o.order_date = :date
            LEFT JOIN erp_finance_schema.sales_payment_tbl p
                   ON p.salesman_id = s.salesman_id AND p.payment_date = :date
            WHERE s.is_active = TRUE
            GROUP BY s.salesman_id, s.full_name, r.route_name, tgt.visit_target
            ORDER BY s.full_name
            """;
        return databaseClient.sql(sql)
                .bind("date", date)
                .map((row, meta) -> new SalesmanActivityRowData(
                        row.get("salesman_id",       String.class),
                        row.get("salesman_name",     String.class),
                        row.get("route_name",        String.class),
                        row.get("visit_target",      Long.class),
                        row.get("visits_today",      Long.class),
                        row.get("orders_today",      Long.class),
                        row.get("collections_today", BigDecimal.class)))
                .all();
    }
}
