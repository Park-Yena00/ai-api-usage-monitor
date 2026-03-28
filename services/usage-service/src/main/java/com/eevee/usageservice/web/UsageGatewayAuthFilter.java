package com.eevee.usageservice.web;

import com.eevee.usageservice.config.UsageGatewayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Ensures HTTP requests to {@code /api/v1/usage/**} come from the API Gateway when
 * {@code usage.gateway.require-auth} is true. Same shared secret as Proxy ({@code PROXY_GATEWAY_SHARED_SECRET}).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UsageGatewayAuthFilter extends OncePerRequestFilter {

    public static final String HDR_GATEWAY_AUTH = "X-Gateway-Auth";

    private final UsageGatewayProperties gatewayProperties;

    public UsageGatewayAuthFilter(UsageGatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/v1/usage")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!gatewayProperties.isRequireAuth()) {
            filterChain.doFilter(request, response);
            return;
        }
        String expected = gatewayProperties.getSharedSecret();
        if (expected == null || expected.isBlank()) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return;
        }
        String incoming = request.getHeader(HDR_GATEWAY_AUTH);
        if (!constantTimeEquals(expected, incoming)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean constantTimeEquals(String expected, String incoming) {
        if (incoming == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = incoming.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        return MessageDigest.isEqual(a, b);
    }
}
