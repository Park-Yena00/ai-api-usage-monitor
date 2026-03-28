package com.eevee.proxyservice.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds {@code X-User-Id} (from Gateway) to the security context for {@code /proxy/**}.
 * Optional {@code X-User-Role} maps to Spring {@code ROLE_*} authorities when present.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LocalUserHeadersWebFilter implements WebFilter {

    private static final String HDR_USER = "X-User-Id";
    private static final String HDR_USER_ROLE = "X-User-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/proxy/")) {
            return chain.filter(exchange);
        }
        String userId = exchange.getRequest().getHeaders().getFirst(HDR_USER);
        if (userId == null || userId.isBlank()) {
            return chain.filter(exchange);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleHeader = exchange.getRequest().getHeaders().getFirst(HDR_USER_ROLE);
        if (roleHeader != null && !roleHeader.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleHeader));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId,
                "N/A",
                authorities
        );
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}
