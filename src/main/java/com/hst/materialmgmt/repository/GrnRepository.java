package com.hst.materialmgmt.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hst.materialmgmt.entity.GrnEntity;
import com.hst.materialmgmt.rowMapper.BaseRowMapper;
import com.hst.materialmgmt.rowMapper.GrnRowMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class GrnRepository extends ParentRepositoryImpl {

    @Autowired
    private GrnRowMapper rowMapper;

    @Override protected String getTableName() { return "rm_grn_tbl"; }
    @Override protected Map<String, Object> getKeyParamMap(String id) { return Map.of("grn_id", id); }
    @SuppressWarnings("unchecked")
    @Override protected <T> BaseRowMapper<T> getRowMapper() { return (BaseRowMapper<T>) rowMapper; }
    @SuppressWarnings("unchecked")
    @Override protected <T> Class<T> getEntityClass() { return (Class<T>) GrnEntity.class; }

    public Flux<GrnEntity> findAllGrns() {
        return databaseClient.sql("""
                SELECT g.* FROM rm_material_schema.rm_grn_tbl g
                ORDER BY g.received_date DESC, g.created_at DESC
                """)
                .map((row, meta) -> rowMapper.apply(row, meta)).all();
    }

    public Mono<GrnEntity> findByGrnId(String grnId) {
        return databaseClient.sql(
                "SELECT * FROM rm_material_schema.rm_grn_tbl WHERE grn_id = :grnId")
                .bind("grnId", grnId)
                .map((row, meta) -> rowMapper.apply(row, meta)).one();
    }

    public Mono<String> nextGrnId() {
        return databaseClient.sql("""
                SELECT s.n FROM generate_series(1,(SELECT COUNT(*)+1 FROM rm_material_schema.rm_grn_tbl)) AS s(n)
                WHERE NOT EXISTS (
                    SELECT 1 FROM rm_material_schema.rm_grn_tbl
                    WHERE grn_id = CONCAT('GRN-', LPAD(s.n::text,6,'0'))
                ) ORDER BY s.n LIMIT 1
                """)
                .map((row, meta) -> row.get(0, Long.class)).one()
                .map(n -> String.format("GRN-%06d", n));
    }

    public Mono<Void> deleteByGrnId(String grnId) {
        return databaseClient.sql(
                "DELETE FROM rm_material_schema.rm_grn_tbl WHERE grn_id = :grnId")
                .bind("grnId", grnId)
                .fetch().rowsUpdated().then();
    }

    // No request class needed — individual params
    public Mono<Void> updateGrn(String grnId, String supplierCode, LocalDate receivedDate,
                                String invoiceNumber, String notes, String status) {
        return databaseClient.sql("""
                UPDATE rm_material_schema.rm_grn_tbl
                   SET supplier_code  = :supplierCode,
                       received_date  = :receivedDate,
                       invoice_number = :invoiceNumber,
                       notes          = :notes,
                       status         = :status,
                       updated_at     = NOW()
                 WHERE grn_id = :grnId
                """)
                .bind("supplierCode",  supplierCode  != null ? supplierCode  : "")
                .bind("receivedDate",  receivedDate  != null ? receivedDate  : LocalDate.now())
                .bind("invoiceNumber", invoiceNumber != null ? invoiceNumber : "")
                .bind("notes",         notes         != null ? notes         : "")
                .bind("status",        status        != null ? status        : "CONFIRMED")
                .bind("grnId",         grnId)
                .fetch().rowsUpdated().then();
    }
}