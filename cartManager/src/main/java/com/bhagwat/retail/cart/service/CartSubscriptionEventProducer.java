package com.bhagwat.retail.cart.service;

import com.bhagwat.retail.cart.dto.CartSubscriptionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartSubscriptionEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(CartSubscriptionEventProducer.class);
    private static final String SUBSCRIPTION_TOPIC = "cart-subscriptions";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a DTO to the Kafka topic.
     * @param dto The DTO to publish.
     */
    public void publishEvent(CartSubscriptionDto dto) {
        // In a real application, this would use KafkaTemplate.
        // For demonstration, we just log the event.
        logger.info("Published event to Kafka topic '{}' for subscription ID: {}", SUBSCRIPTION_TOPIC, dto.getId());
    }

    public void send(String topic, Object event) {
        log.info("Sending message to topic {}: {}", topic, event.getClass());
        kafkaTemplate.send(topic, event);
    }

}
