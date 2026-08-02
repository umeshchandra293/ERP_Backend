package com.hst.materialmgmt.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.SalesOrderItemEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.SalesOrderItemRowMapper;

import reactor.core.publisher.Flux;

@Repository
public class SalesOrderItemRepository extends ParentRepositoryImpl {

    @Autowired private SalesOrderItemRowMapper rowMapper;

    @Override protected String getTableName() { return "sales_order_item_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("item_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) SalesOrderItemEntity.class; }

    public Flux<SalesOrderItemEntity> findByOrderId(String orderId) {
        String sql = "SELECT * FROM erp_finance_schema.sales_order_item_tbl WHERE order_id = :orderId";
        return databaseClient.sql(sql)
                .bind("orderId", orderId)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }
}
