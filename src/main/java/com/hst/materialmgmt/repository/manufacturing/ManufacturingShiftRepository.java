package com.hst.materialmgmt.repository.manufacturing;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.hst.materialmgmt.entity.manufacturing.ManufacturingShiftEntity;
import com.hst.materialmgmt.repository.ParentRepositoryImpl;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.manufacturing.ManufacturingShiftRowMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ManufacturingShiftRepository extends ParentRepositoryImpl {

    @Autowired private ManufacturingShiftRowMapper rowMapper;

    @Override protected String getTableName() { return "manufacturing_shift_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("shift_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) ManufacturingShiftEntity.class; }

    public Flux<ManufacturingShiftEntity> findAllShifts() {
        return databaseClient.sql("""
            SELECT * FROM rm_material_schema.manufacturing_shift_tbl
            ORDER BY shift_date DESC, created_at DESC
            """)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

public Mono<String> nextShiftId() {
    return databaseClient.sql("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(shift_id FROM 7) AS INTEGER)), 0) + 1 AS next_num
        FROM rm_material_schema.manufacturing_shift_tbl
        """)
            .map((row, meta) -> row.get("next_num", Integer.class)).one()
            .map(n -> String.format("SHIFT-%06d", n));
}
}