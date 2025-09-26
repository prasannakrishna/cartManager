package com.bhagwat.retail.cart.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RefreshScope
public class KafkaTopicConfig {

    // --- Cart Subscription Topic Properties ---
    @Value("${app.kafka.topics.cart-events.name}")
    private String cartSubscriptionTopicName;
    @Value("${app.kafka.topics.cart-events.partitions}")
    private int cartSubscriptionTopicPartitions;
    @Value("${app.kafka.topics.cart-events.replicas}")
    private short cartSubscriptionTopicReplicas;
    @Value("${app.kafka.topics.cart-events.retention-ms}")
    private String cartSubscriptionTopicRetentionMs;
    @Value("${app.kafka.topics.cart-events.cleanup-policy}")
    private String cartSubscriptionTopicCleanupPolicy;


    @Bean
    @RefreshScope
    public NewTopic cartSubscriptionEventsTopic() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", cartSubscriptionTopicRetentionMs);
        configs.put("cleanup.policy", cartSubscriptionTopicCleanupPolicy);
        // Add more custom topic configurations here if needed, e.g., "max.message.bytes"

        return TopicBuilder.name(cartSubscriptionTopicName)
                .partitions(cartSubscriptionTopicPartitions)
                .replicas(cartSubscriptionTopicReplicas)
                .configs(configs)
                .build();
    }
}