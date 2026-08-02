package com.hst.materialmgmt.controller;

import java.util.List;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import com.hst.api.StockMovementApi;
import com.hst.api.model.DashboardSummary;
import com.hst.api.model.StockMovement;
import com.hst.api.model.TrendPoint;
import com.hst.api.model.WastageReason;
import com.hst.materialmgmt.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/material/mgmt")
@Tag(name = "Stock Movement API", description = "Inventory ledger operations")
public class StockMovementController implements StockMovementApi {

    private static final Logger log = LoggerFactory.getLogger(StockMovementController.class);

    @Autowired private StockMovementService service;

    // ── Manual adjust request — bypasses generated model validation ───────────
    public record ManualAdjustRequest(
        String  materialId,
        double  quantity,      // always positive
        boolean isReduction,   // true = reduce stock, false = add stock
        String  reasonCode,
        String  notes
    ) {}

    // ── Original OpenAPI endpoints (/stock-movements) ────────────────────────

    @Override
    public Mono<ResponseEntity<Flux<StockMovement>>> getAllMovements(
            String materialId, String movementType,
            LocalDate fromDate, LocalDate toDate,
            ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(
                service.findFiltered(materialId, movementType, fromDate, toDate)));
    }

    @Override
    public Mono<ResponseEntity<Flux<StockMovement>>> recordBatchMovements(
            Flux<StockMovement> stockMovement, ServerWebExchange exchange) {
        return stockMovement.collectList()
                .map(list -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(service.createBatch(list)))
                .doOnError(e -> log.error("Batch movement record failed", e));
    }

    // ── GET /material/mgmt/inventory/movements ────────────────────────────────

    @GetMapping("/inventory/movements")
    public Mono<ResponseEntity<Flux<StockMovement>>> getMovements(
            @RequestParam(required = false) String materialId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return Mono.just(ResponseEntity.ok(
                service.findFiltered(materialId, movementType, fromDate, toDate)));
    }

    // ── POST /material/mgmt/inventory/movements ───────────────────────────────
    // Used by manufacturing shift entry (CONSUMPTION movements)

    @PostMapping("/inventory/movements")
    public Mono<ResponseEntity<StockMovement>> createMovement(
            @RequestBody StockMovement movement) {
        return service.createBatch(java.util.List.of(movement))
                .next()
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .doOnError(e -> log.error("Movement creation failed: {}", e.getMessage()));
    }

    // ── POST /material/mgmt/inventory/adjust ──────────────────────────────────
    // Manual stock adjustment — add or reduce without touching wastage/consumed KPIs
    // Uses a custom request model to bypass generated StockMovement validation

    @PostMapping("/inventory/adjust")
    @Operation(summary = "Manual stock adjustment — add or reduce stock quantity")
    public Mono<ResponseEntity<Void>> manualAdjust(
            @RequestBody ManualAdjustRequest req) {
        return service.manualAdjust(
                req.materialId(), req.quantity(),
                req.isReduction(), req.reasonCode(), req.notes())
                .thenReturn(ResponseEntity.<Void>ok().<Void>build())
                .onErrorResume(e -> {
                    log.error("Manual adjust failed for {}: {}", req.materialId(), e.getMessage());
                    return Mono.just(ResponseEntity.<Void>status(HttpStatus.BAD_REQUEST).build());
                });
    }


    @GetMapping("/inventory/dashboard")
    public Mono<ResponseEntity<DashboardSummary>> getDashboard(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusDays(30);
        LocalDate to   = toDate   != null ? LocalDate.parse(toDate)   : LocalDate.now();
        return service.getDashboardSummary(from, to).map(ResponseEntity::ok);
    }

    @GetMapping("/inventory/trend")
    public ResponseEntity<Flux<TrendPoint>> getTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getTrend(days));
    }

    @GetMapping("/inventory/wastage-breakdown")
    public ResponseEntity<Flux<WastageReason>> getWastageBreakdown(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getWastageBreakdown(days));
    }

    // ── GET movements by shift/reference ─────────────────────────────────────

    @GetMapping("/inventory/movements/by-shift/{shiftId}")
    @Operation(summary = "Get all consumption movements for a shift")
    public Mono<ResponseEntity<List<StockMovement>>> getMovementsByShift(
            @PathVariable String shiftId,
            @RequestParam(required = false) String movementType) {
        return service.findByReferenceId(shiftId, movementType)
                .collectList()
                .map(ResponseEntity::ok);
    }

    // ── PUT movement — edit quantity ──────────────────────────────────────────

    @PutMapping("/inventory/movements/{movementId}")
    @Operation(summary = "Update movement quantity")
    public Mono<ResponseEntity<Void>> updateMovement(
            @PathVariable String movementId,
            @RequestBody MovementUpdateRequest req) {
        return service.updateMovementQuantity(movementId, req.quantity())
                .thenReturn(ResponseEntity.<Void>ok().<Void>build())
                .onErrorResume(e -> {
                    log.error("Movement update failed {}: {}", movementId, e.getMessage());
                    return Mono.just(ResponseEntity.<Void>status(HttpStatus.BAD_REQUEST).build());
                });
    }

    // ── DELETE movement ───────────────────────────────────────────────────────

    @DeleteMapping("/inventory/movements/{movementId}")
    @Operation(summary = "Delete a movement record")
    public Mono<ResponseEntity<Void>> deleteMovement(@PathVariable String movementId) {
        return service.deleteMovement(movementId)
                .thenReturn(ResponseEntity.<Void>noContent().<Void>build())
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.<Void>status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    // ── Request record ────────────────────────────────────────────────────────

    public record MovementUpdateRequest(double quantity) {}
}