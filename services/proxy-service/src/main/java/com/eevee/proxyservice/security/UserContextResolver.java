package com.eevee.proxyservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class UserContextResolver {

    private static final String HDR_USER = "X-User-Id";
    private static final String HDR_PLATFORM_USER = "X-Platform-User-Id";
    private static final String HDR_USER_ROLE = "X-User-Role";
    private static final String HDR_ORG = "X-Org-Id";
    private static final String HDR_TEAM = "X-Team-Id";
    private static final String HDR_CORRELATION = "X-Correlation-Id";

    public Mono<UserContext> fromExchange(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HDR_CORRELATION);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        String finalCorrelationId = correlationId;
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> mapAuthentication(ctx.getAuthentication(), exchange, finalCorrelationId))
                .switchIfEmpty(Mono.defer(() -> mapAuthentication(null, exchange, finalCorrelationId)));
    }

    private Mono<UserContext> mapAuthentication(
            Authentication auth,
            ServerWebExchange exchange,
            String correlationId
    ) {
        String platformUserId = emptyToNull(exchange.getRequest().getHeaders().getFirst(HDR_PLATFORM_USER));
        String role = emptyToNull(exchange.getRequest().getHeaders().getFirst(HDR_USER_ROLE));
        String org = emptyToNull(exchange.getRequest().getHeaders().getFirst(HDR_ORG));
        String team = emptyToNull(exchange.getRequest().getHeaders().getFirst(HDR_TEAM));

        String subjectHeader = exchange.getRequest().getHeaders().getFirst(HDR_USER);
        if (subjectHeader != null && !subjectHeader.isBlank()) {
            return Mono.just(new UserContext(subjectHeader, platformUserId, role, org, team, correlationId));
        }
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String p && !p.isBlank()) {
            return Mono.just(new UserContext(p, platformUserId, role, org, team, correlationId));
        }
        return Mono.error(new IllegalStateException("Missing X-User-Id (from Gateway)"));
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }
}
