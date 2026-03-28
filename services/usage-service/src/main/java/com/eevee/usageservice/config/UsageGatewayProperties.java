package com.eevee.usageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trust boundary with API Gateway for Usage HTTP ({@code docs/contracts/gateway-proxy.md} §10).
 */
@ConfigurationProperties(prefix = "usage.gateway")
public class UsageGatewayProperties {

    /**
     * When true, {@code X-Gateway-Auth} must match {@link #sharedSecret}.
     */
    private boolean requireAuth = false;

    private String sharedSecret = "";

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
}
