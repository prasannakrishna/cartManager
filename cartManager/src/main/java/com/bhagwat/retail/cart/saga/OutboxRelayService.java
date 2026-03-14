package com.bhagwat.retail.cart.saga;

import com.bhagwat.retail.cart.entity.OutboxEvent;
import com.bhagwat.retail.cart.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Transactional Outbox Relay.
 * Polls the outbox_events table every 2 seconds and publishes PENDING events to Kafka.
 * This decouples DB commit from Kafka publish — if Kafka is temporarily down,
 * events are not lost; they stay PENDING and are retried.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> rawKafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEvents();
        if (pending.isEmpty()) return;

        log.debug("Outbox relay: {} PENDING events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        event.getTopic(),
                        null,
                        event.getMessageKey(),
                        event.getPayload()
                );
                // Preserve __TypeId__ header so consumers can deserialize correctly
                record.headers().add(new RecordHeader(
                        "__TypeId__",
                        event.getEventType().getBytes(StandardCharsets.UTF_8)
                ));

                rawKafkaTemplate.send(record).get(); // synchronous — confirm delivery

                event.setStatus(OutboxEvent.Status.PUBLISHED);
                event.setPublishedAt(Instant.now());
                log.info("Outbox relayed eventId={} topic={}", event.getEventId(), event.getTopic());

            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    log.error("Outbox event permanently failed after 5 retries: eventId={}", event.getEventId(), e);
                } else {
                    log.warn("Outbox relay failed, will retry (attempt {}): eventId={}", event.getRetryCount(), event.getEventId(), e);
                }
            }
            outboxEventRepository.save(event);
        }
    }
}
