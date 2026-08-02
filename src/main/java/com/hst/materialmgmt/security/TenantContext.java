package com.hst.materialmgmt.security;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * Holds the current request's companyId inside the Reactor Context.
 * WebFlux is non-blocking/multi-threaded, so a normal ThreadLocal would
 * leak between requests — Reactor Context is the correct mechanism here.
 *
 * Usage in a repository or service:
 *   TenantContext.getCompanyId()
 *       .flatMap(companyId -> databaseClient.sql("... WHERE company_id = :companyId ...")...)
 */
public class TenantContext {

    private static final String COMPANY_ID_KEY = "companyId";

    /** Call this from the JWT filter to inject the companyId into the reactor chain. */
    public static Context withCompanyId(Context ctx, String companyId) {
        return ctx.put(COMPANY_ID_KEY, companyId);
    }

    /** Call this from any repository/service to read the current request's companyId. */
    public static Mono<String> getCompanyId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(COMPANY_ID_KEY)) {
                return Mono.just(ctx.get(COMPANY_ID_KEY));
            }
            return Mono.error(new IllegalStateException(
                    "No companyId found in Reactor Context — request may not have gone through JwtAuthFilter"));
        });
    }

    /** Non-reactive helper, for cases where you already have a ContextView. */
    public static String getCompanyIdOrNull(ContextView ctx) {
        return ctx.hasKey(COMPANY_ID_KEY) ? ctx.get(COMPANY_ID_KEY) : null;
    }
}