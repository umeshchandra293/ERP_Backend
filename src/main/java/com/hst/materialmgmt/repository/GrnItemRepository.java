package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.GrnItemEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.GrnItemRowMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class GrnItemRepository extends ParentRepositoryImpl {

    @Autowired private GrnItemRowMapper rowMapper;

    @Override protected String getTableName() { return "rm_grn_item_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) {
        return Map.of("grn_item_id", id);
    }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) GrnItemEntity.class; }

    public Flux<GrnItemEntity> findByGrnId(String grnId) {
        return databaseClient.sql("""
                SELECT gi.*
                FROM rm_material_schema.rm_grn_item_tbl gi
                WHERE gi.grn_id = :grnId
                """)
                .bind("grnId", grnId)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<GrnItemEntity> findByGrnItemId(String grnItemId) {
        return databaseClient.sql(
                "SELECT * FROM rm_material_schema.rm_grn_item_tbl WHERE grn_item_id = :grnItemId")
                .bind("grnItemId", grnItemId)
                .map((row, meta) -> rowMapper.apply(row, meta)).one();
    }

    public Mono<String> nextGrnItemId() {
        return databaseClient
                .sql("SELECT nextval('rm_material_schema.grn_item_code_seq')")
                .map((row, meta) -> row.get(0, Long.class)).one()
                .map(n -> String.format("GRNI-%06d", n));
    }

    public Mono<Void> deleteByGrnId(String grnId) {
        return databaseClient.sql(
                "DELETE FROM rm_material_schema.rm_grn_item_tbl WHERE grn_id = :grnId")
                .bind("grnId", grnId)
                .fetch().rowsUpdated().then();
    }

    // No request class needed — individual params
    public Mono<Void> updateGrnItem(String grnItemId, Double orderedQty,
                                    Double receivedQty, Double unitCost) {
        return databaseClient.sql("""
                UPDATE rm_material_schema.rm_grn_item_tbl
                   SET ordered_qty  = :orderedQty,
                       received_qty = :receivedQty,
                       unit_cost    = :unitCost,
                       updated_at   = NOW()
                 WHERE grn_item_id = :grnItemId
                """)
                .bind("orderedQty",  orderedQty  != null ? BigDecimal.valueOf(orderedQty)  : BigDecimal.ZERO)
                .bind("receivedQty", receivedQty != null ? BigDecimal.valueOf(receivedQty) : BigDecimal.ZERO)
                .bind("unitCost",    unitCost    != null ? BigDecimal.valueOf(unitCost)    : BigDecimal.ZERO)
                .bind("grnItemId",   grnItemId)
                .fetch().rowsUpdated().then();
    }
}