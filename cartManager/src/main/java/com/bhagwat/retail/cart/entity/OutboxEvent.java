package com.bhagwat.retail.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Transactional Outbox table.
 * Events are written here in the same DB transaction as the business entity change.
 * A relay (@Scheduled) reads PENDING rows and publishes them to Kafka,
 * then marks them PUBLISHED — decoupling DB commit from Kafka publish atomically.
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    public enum Status { PENDING, PUBLISHED, FAILED }

    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;

    /** Kafka topic to publish to */
    @Column(name = "topic", nullable = false)
    private String topic;

    /** Kafka message key (e.g. customerId, communityId) */
    @Column(name = "message_key")
    private String messageKey;

    /** Fully-qualified event type name (used for __TypeId__ header) */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /** JSON-serialized event payload */
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
}
