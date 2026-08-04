package com.hst.materialmgmt.repository.manufacturing;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.hst.materialmgmt.entity.manufacturing.ManufacturingBatchEntity;
import com.hst.materialmgmt.repository.ParentRepositoryImpl;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.manufacturing.ManufacturingBatchRowMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ManufacturingBatchRepository extends ParentRepositoryImpl {

    @Autowired private ManufacturingBatchRowMapper rowMapper;

    @Override protected String getTableName() { return "manufacturing_batch_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("batch_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) ManufacturingBatchEntity.class; }

    public Flux<ManufacturingBatchEntity> findByShiftId(String shiftId) {
        return databaseClient.sql("""
            SELECT b.*, p.name AS product_name, p.sku
            FROM rm_material_schema.manufacturing_batch_tbl b
            LEFT JOIN rm_material_schema.product_tbl p ON p.product_id = b.product_id
            WHERE b.shift_id = :shiftId
            """)
                .bind("shiftId", shiftId)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    // Was previously gap-filling via generate_series — after any TRUNCATE or
    // partial delete, it would reissue an old batch_id whose orphaned
    // stock-movement/reference data might still be sitting in other tables,
    // silently reattaching stale data to a brand-new batch. Same root cause
    // and same fix as ManufacturingShiftRepository.nextShiftId(): always
    // increment past the highest number ever issued, never reuse a gap.
    public Mono<String> nextBatchId() {
        return databaseClient.sql("""
            SELECT COALESCE(MAX(CAST(SUBSTRING(batch_id FROM 7) AS INTEGER)), 0) + 1 AS next_num
            FROM rm_material_schema.manufacturing_batch_tbl
            """)
                .map((row, meta) -> row.get("next_num", Integer.class)).one()
                .map(n -> String.format("BATCH-%06d", n));
    }

    public Mono<ManufacturingBatchEntity> findByBatchId(String batchId) {
        return databaseClient.sql(
            "SELECT * FROM rm_material_schema.manufacturing_batch_tbl WHERE batch_id = :id")
            .bind("id", batchId)
            .map((row, meta) -> rowMapper.apply(row, meta)).one();
    }

    public Mono<Void> deleteByShiftId(String shiftId) {
        return databaseClient.sql(
            "DELETE FROM rm_material_schema.manufacturing_batch_tbl WHERE shift_id = :shiftId")
            .bind("shiftId", shiftId)
            .fetch().rowsUpdated().then();
    }
}