package com.eevee.usageservice.domain;

import com.eevee.usage.events.AiProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Persists {@link com.eevee.usage.events.UsageRecordedEvent} for authoritative usage ledger (pattern A).
 */
@Entity
@Table(name = "usage_recorded_log")
public class UsageRecordedLogEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false)
    private Instant occurredAt;

    private String correlationId;

    @Column(nullable = false)
    private String userId;

    private String organizationId;

    private String teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiProvider provider;

    private String model;

    private Long promptTokens;

    private Long completionTokens;

    private Long totalTokens;

    @Column(precision = 19, scale = 4)
    private BigDecimal estimatedCost;

    private String requestPath;

    private String upstreamHost;

    private Boolean streaming;

    @Column(nullable = false)
    private Instant persistedAt;

    protected UsageRecordedLogEntity() {
    }

    public UsageRecordedLogEntity(
            UUID eventId,
            Instant occurredAt,
            String correlationId,
            String userId,
            String organizationId,
            String teamId,
            AiProvider provider,
            String model,
            Long promptTokens,
            Long completionTokens,
            Long totalTokens,
            BigDecimal estimatedCost,
            String requestPath,
            String upstreamHost,
            Boolean streaming,
            Instant persistedAt
    ) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.userId = userId;
        this.organizationId = organizationId;
        this.teamId = teamId;
        this.provider = provider;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.estimatedCost = estimatedCost;
        this.requestPath = requestPath;
        this.upstreamHost = upstreamHost;
        this.streaming = streaming;
        this.persistedAt = persistedAt;
    }

    public UUID getEventId() {
        return eventId;
    }
}
