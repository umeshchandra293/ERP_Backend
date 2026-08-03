package com.hst.materialmgmt.controller.rawMaterail;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import com.hst.api.RawMaterialApi;
import com.hst.api.model.RawMaterial;
import com.hst.materialmgmt.service.RawMaterialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/material/mgmt")
@Tag(name = "Raw Material API")
public class RawMaterialController extends com.hst.materialmgmt.controller.BaseController implements RawMaterialApi {

    @Autowired private RawMaterialService rawMaterialService;

    @Override
    public Mono<ResponseEntity<Flux<RawMaterial>>> getAllMaterials(
            String category, Boolean isActive, ServerWebExchange exchange) {
        return findAll(rawMaterialService, exchange)
                .cast(RawMaterial.class)
                .collectList()
                .map(list -> ResponseEntity.ok(Flux.fromIterable(list)));
    }

    @Override
    public Mono<ResponseEntity<RawMaterial>> getMaterialById(
            String materialId, ServerWebExchange exchange) {
        return findByKey(rawMaterialService, materialId, exchange)
                .cast(RawMaterial.class)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<RawMaterial>> createMaterial(
            Mono<RawMaterial> rawMaterial, ServerWebExchange exchange) {
        return create(rawMaterialService, rawMaterial.cast(Object.class), exchange)
                .cast(RawMaterial.class)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @Override
    public Mono<ResponseEntity<RawMaterial>> updateMaterial(
            String materialId, Mono<RawMaterial> rawMaterial, ServerWebExchange exchange) {
        return update(rawMaterialService, materialId, rawMaterial.cast(Object.class), exchange)
                .cast(RawMaterial.class)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ── Delete — bypasses BaseController.delete() entirely ──────────────────
    // BaseController.delete() swallows every error into a plain 500 before it
    // ever returns, which meant our onErrorResume here never saw a real error
    // signal to catch. So this calls the service directly instead, keeping the
    // error signal alive so we CAN catch the FK violation and translate it into
    // a proper 409 with a helpful message.

    @Override
    public Mono<ResponseEntity<Void>> deleteMaterial(
            String materialId, ServerWebExchange exchange) {

        if (materialId == null || materialId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Material ID cannot be null or empty"));
        }

        return rawMaterialService.deleteFullHierarchy(materialId)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    System.err.println("=== DELETE ERROR DEBUG ===");
                    System.err.println("Exception class: " + e.getClass().getName());
                    System.err.println("Exception message: " + e.getMessage());
                    Throwable cause = e.getCause();
                    int depth = 0;
                    while (cause != null && depth < 5) {
                        System.err.println("Cause[" + depth + "] class: " + cause.getClass().getName());
                        System.err.println("Cause[" + depth + "] message: " + cause.getMessage());
                        cause = cause.getCause();
                        depth++;
                    }
                    System.err.println("==========================");

                    String msg = buildFullMessage(e);
                    boolean isForeignKeyViolation =
                            msg.contains("23503") ||
                            msg.toLowerCase().contains("foreign key") ||
                            msg.toLowerCase().contains("violates") ||
                            msg.toLowerCase().contains("still referenced");

                    if (isForeignKeyViolation) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Cannot delete — this material is referenced by existing GRN "
                                + "or stock movement records (foreign key constraint). "
                                + "Deactivate it instead."));
                    }
                    return Mono.error(e);
                });
    }

    private String buildFullMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable current = t;
        int depth = 0;
        while (current != null && depth < 6) {
            if (current.getMessage() != null) sb.append(current.getMessage()).append(" | ");
            current = current.getCause();
            depth++;
        }
        return sb.toString();
    }

    @GetMapping("/rawmaterial/categories")
    public Mono<ResponseEntity<List<String>>> getCategories() {
        return Mono.just(ResponseEntity.ok(List.of(
                "PREFORMS", "CHEMICALS", "CAPS", "LABELS", "PACKAGING", "OTHER"
        )));
    }

    @GetMapping("/rawmaterial/uoms")
    public Mono<ResponseEntity<List<String>>> getUoms() {
        return Mono.just(ResponseEntity.ok(List.of(
                "KG", "GM", "LTR", "ML", "PCS", "ROLL", "BAG", "BOX"
        )));
    }
}