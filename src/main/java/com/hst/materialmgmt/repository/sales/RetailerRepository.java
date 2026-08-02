package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.RetailerEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.RetailerRowMapper;
import com.hst.materialmgmt.service.SalesKpisRow;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class RetailerRepository extends ParentRepositoryImpl {

    @Autowired private RetailerRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_retailer_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) {
        return Map.of("retailer_id", id);
    }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) RetailerEntity.class; }

    public Flux<RetailerEntity> findBySalesmanId(String salesmanId) {
        String sql = "SELECT r.* FROM erp_finance_schema.sales_retailer_tbl r " +
                     "WHERE r.assigned_salesman_id = :salesmanId AND r.is_active = TRUE " +
                     "ORDER BY r.shop_name";
        return databaseClient.sql(sql)
                .bind("salesmanId", salesmanId)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Flux<RetailerEntity> findFiltered(String salesmanId, String area, Boolean isActive) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM erp_finance_schema.sales_retailer_tbl WHERE 1=1");
        if (salesmanId != null) sql.append(" AND assigned_salesman_id = :salesmanId");
        if (area != null)       sql.append(" AND area = :area");
        if (isActive != null)   sql.append(" AND is_active = :isActive");
        sql.append(" ORDER BY shop_name");

        var spec = databaseClient.sql(sql.toString());
        if (salesmanId != null) spec = spec.bind("salesmanId", salesmanId);
        if (area != null)       spec = spec.bind("area", area);
        if (isActive != null)   spec = spec.bind("isActive", isActive);

        return spec.map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<RetailerEntity> updateBalance(String retailerId, BigDecimal newBalance) {
        String sql = "UPDATE erp_finance_schema.sales_retailer_tbl " +
                     "SET current_balance = :balance, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE retailer_id = :id";
        return databaseClient.sql(sql)
                .bind("balance", newBalance)
                .bind("id", retailerId)
                .fetch().rowsUpdated()
                .then(findRetailerById(retailerId));
    }

    public Mono<RetailerEntity> findRetailerById(String id) {
        String sql = "SELECT * FROM erp_finance_schema.sales_retailer_tbl WHERE retailer_id = :id";
        return databaseClient.sql(sql)
                .bind("id", id)
                .map((row, meta) -> rowMapper.apply(row, meta)).one();
    }

    /**
     * Returns retailers with salesman full_name resolved.
     * Returns Object[] rows: retailer fields + salesman_full_name at index[12]
     */
    public Flux<Object[]> findFilteredWithSalesmanName(
            String salesmanId, String area, Boolean isActive) {
        StringBuilder sql = new StringBuilder("""
            SELECT r.*,
                   COALESCE(s.full_name, '') AS salesman_full_name
            FROM erp_finance_schema.sales_retailer_tbl r
            LEFT JOIN erp_finance_schema.sales_salesman_tbl s
                   ON s.salesman_id = r.assigned_salesman_id
            WHERE 1=1
            """);
        if (salesmanId != null) sql.append(" AND r.assigned_salesman_id = :salesmanId");
        if (area != null)       sql.append(" AND r.area = :area");
        if (isActive != null)   sql.append(" AND r.is_active = :isActive");
        sql.append(" ORDER BY r.shop_name");

        var spec = databaseClient.sql(sql.toString());
        if (salesmanId != null) spec = spec.bind("salesmanId", salesmanId);
        if (area != null)       spec = spec.bind("area", area);
        if (isActive != null)   spec = spec.bind("isActive", isActive);

        return spec.map((row, meta) -> new Object[]{
                row.get("retailer_id",           String.class),
                row.get("shop_name",             String.class),
                row.get("owner_name",            String.class),
                row.get("phone",                 String.class),
                row.get("address",               String.class),
                row.get("area",                  String.class),
                row.get("gps_lat",               BigDecimal.class),
                row.get("gps_lng",               BigDecimal.class),
                row.get("assigned_salesman_id",  String.class),
                row.get("credit_limit",          BigDecimal.class),
                row.get("current_balance",       BigDecimal.class),
                row.get("is_active",             Boolean.class),
                row.get("salesman_full_name",     String.class),
        }).all();
    }

    public Mono<SalesKpisRow> findKpiTotals() {
        String sql = """
            SELECT
                COUNT(*)                                              AS total_retailers,
                SUM(CASE WHEN is_active THEN 1 ELSE 0 END)           AS active_retailers,
                COALESCE(SUM(current_balance), 0)                    AS total_receivables,
                SUM(CASE WHEN current_balance > 0 THEN 1 ELSE 0 END) AS outstanding_shops,
                SUM(CASE WHEN current_balance > 15000 THEN 1 ELSE 0 END) AS critical_accounts
            FROM erp_finance_schema.sales_retailer_tbl
            """;
        return databaseClient.sql(sql)
                .map((row, meta) -> new SalesKpisRow(
                        row.get("total_retailers",    Long.class),
                        row.get("active_retailers",   Long.class),
                        row.get("total_receivables",  BigDecimal.class),
                        row.get("outstanding_shops",  Long.class),
                        row.get("critical_accounts",  Long.class)))
                .one();
    }
}
