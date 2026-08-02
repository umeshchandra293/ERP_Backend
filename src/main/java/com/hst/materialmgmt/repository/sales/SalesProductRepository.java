package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesProductEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesProductRowMapper;

import reactor.core.publisher.Flux;

@Repository
public class SalesProductRepository extends ParentRepositoryImpl {

    @Autowired private SalesProductRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_product_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("product_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesProductEntity.class; }

    /**
     * Returns all active products with retailer-specific price applied if it exists.
     * Falls back to base_price if no custom pricing row found.
     */
    public Flux<Object[]> findProductsWithPricingForRetailer(String retailerId) {
        String sql = """
            SELECT
                p.product_id,
                p.product_name,
                p.sku,
                p.base_price,
                p.unit,
                p.is_active,
                COALESCE(rp.custom_price, p.base_price) AS effective_price
            FROM erp_finance_schema.sales_product_tbl p
            LEFT JOIN erp_finance_schema.sales_retailer_pricing_tbl rp
                   ON rp.product_id = p.product_id
                  AND rp.retailer_id = :retailerId
            WHERE p.is_active = TRUE
            ORDER BY p.product_name
            """;
        return databaseClient.sql(sql)
                .bind("retailerId", retailerId)
                .map((row, meta) -> new Object[]{
                        row.get("product_id",    String.class),
                        row.get("product_name",  String.class),
                        row.get("sku",           String.class),
                        row.get("base_price",    BigDecimal.class),
                        row.get("effective_price", BigDecimal.class),
                        row.get("unit",          String.class),
                        row.get("is_active",     Boolean.class)
                }).all();
    }
}
