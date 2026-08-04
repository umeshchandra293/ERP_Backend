package com.hst.materialmgmt.controller.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.hst.api.ProductsApi;
import com.hst.api.model.Product;
import com.hst.materialmgmt.controller.BaseController;
import com.hst.materialmgmt.service.product.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/material/mgmt")
@Tag(name = "Product API")
public class ProductController extends BaseController implements ProductsApi {

    @Autowired private ProductService productService;

    @Override
    public Mono<ResponseEntity<Product>> getAllProducts(ServerWebExchange exchange) {
        // Was previously a stub returning ResponseEntity.ok(null) — never called the service.
        // The generated schema for this operation only allows a single Product in the 200 body,
        // which doesn't fit "list all" semantics anyway. /products/all below is the real list
        // endpoint; this just stops returning a fake null body.
        return findAll(productService, exchange)
                .cast(Product.class)
                .next()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok().build());
    }

    @GetMapping("/products/all")
    public Mono<ResponseEntity<List<Product>>> getAllProductsList(ServerWebExchange exchange) {
        return findAll(productService, exchange)
                .cast(Product.class).collectList()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(List.of()));
    }

    @Override
    public Mono<ResponseEntity<Product>> getProductById(
            String productId, ServerWebExchange exchange) {
        return findByKey(productService, productId, exchange)
                .cast(Product.class).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Void>> createProduct(
            Mono<Product> product, ServerWebExchange exchange) {
        return create(productService, product.cast(Object.class), exchange)
                .cast(Product.class)
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).<Void>build());
    }

    @Override
    public Mono<ResponseEntity<Void>> updateProduct(
            String productId, Mono<Product> product, ServerWebExchange exchange) {
        return update(productService, productId, product.cast(Object.class), exchange)
                .cast(Product.class)
                .map(p -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // BaseController.delete() catches every exception and returns a bare 500, and
    // BaseServiceImpl.deleteFullHierarchy() re-wraps the real Postgres FK violation
    // inside a generic RuntimeException, one level deeper in the cause chain.
    // So: call deleteFullHierarchy() directly here, walk the full cause chain to
    // find the real SQLState, and throw a ResponseStatusException so
    // GlobalExceptionHandler formats a proper message for the frontend instead of
    // an empty 500/409.
    @Override
    public Mono<ResponseEntity<Void>> deleteProduct(String productId, ServerWebExchange exchange) {
        if (productId == null || productId.isEmpty()) {
            return Mono.error(
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id cannot be null or empty"));
        }

        return productService.deleteFullHierarchy(productId)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    String fullMessage = buildFullMessage(e);

                    if (fullMessage.contains("23503") || fullMessage.toLowerCase().contains("foreign key")) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Cannot delete product " + productId
                                        + " — it is still referenced elsewhere (e.g. pricing, discounts, or "
                                        + "GRN/inventory records). Remove those references first."));
                    }

                    if (fullMessage.toLowerCase().contains("not found")) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Product not found: " + productId));
                    }

                    // Anything else: let it propagate as-is so GlobalExceptionHandler's generic
                    // handler surfaces the real message instead of masking it.
                    return Mono.error(e);
                });
    }

    private String buildFullMessage(Throwable e) {
        StringBuilder sb = new StringBuilder();
        Throwable t = e;
        while (t != null) {
            if (t.getMessage() != null) {
                sb.append(t.getMessage()).append(" | ");
            }
            t = t.getCause();
        }
        return sb.toString();
    }
}