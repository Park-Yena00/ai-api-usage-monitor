package com.eevee.usageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "usage.rabbit")
public class UsageRabbitProperties {

    /**
     * Must match {@code proxy.rabbit.usage-exchange} in proxy-service.
     */
    private String exchange = "usage.events";

    /**
     * Must match {@code proxy.rabbit.usage-routing-key} in proxy-service.
     */
    private String routingKey = "usage.recorded";

    /**
     * Dedicated queue for this service (pattern A — fan-out, independent DB).
     */
    private String queue = "usage-service.queue";

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
