package com.bhagwat.retail.cart.service;
import com.bhagwat.retail.cart.dto.CartSubscriptionCreatedEvent;
import com.bhagwat.retail.cart.dto.CartSubscriptionDeletedEvent;
import com.bhagwat.retail.cart.dto.CartSubscriptionDto;
import com.bhagwat.retail.cart.dto.CartSubscriptionUpdatedEvent;
import com.bhagwat.retail.cart.entity.CartSubscriptionDocument;
import com.bhagwat.retail.cart.entity.SubscriptionItem;
import com.bhagwat.retail.cart.repository.CartSubscriptionDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartSubscriptionEventConsumer {
    private final CartSubscriptionDocumentRepository mongoRepository;

    @KafkaListener(topics = "cart-subscriptions-topic", groupId = "cart-subscription-group")
    public void consumeSubscriptionEvents(ConsumerRecord<String, Object> record) {

        Object event = record.value();
        log.info("Received Kafka event: {}", event);
        System.out.println(event.getClass().getName());
        if (event instanceof CartSubscriptionCreatedEvent) {
            CartSubscriptionCreatedEvent createdEvent = (CartSubscriptionCreatedEvent) event;
            CartSubscriptionDocument document = toMongoDocument(createdEvent.getSubscriptionDto());
            document.setId(createdEvent.getId());
            mongoRepository.save(document);
            log.info("Created MongoDB document for subscription ID: {}", createdEvent.getId());
        } else if (event instanceof CartSubscriptionUpdatedEvent) {
            CartSubscriptionUpdatedEvent updatedEvent = (CartSubscriptionUpdatedEvent) event;
            Optional<CartSubscriptionDocument> optionalDocument = mongoRepository.findById(updatedEvent.getId());
            optionalDocument.ifPresent(document -> {
                updateMongoDocument(document, updatedEvent.getSubscriptionDto());
                mongoRepository.save(document);
                log.info("Updated MongoDB document for subscription ID: {}", updatedEvent.getId());
            });
        } else if (event instanceof CartSubscriptionDeletedEvent) {
            CartSubscriptionDeletedEvent deletedEvent = (CartSubscriptionDeletedEvent) event;
            mongoRepository.deleteById(deletedEvent.getId());
            log.info("Deleted MongoDB document for subscription ID: {}", deletedEvent.getId());
        }
    }

    private CartSubscriptionDocument toMongoDocument(Object dto) {
        if (!(dto instanceof CartSubscriptionDto)) {
            throw new IllegalArgumentException("Invalid DTO type provided to converter.");
        }
        CartSubscriptionDto subscriptionDto = (CartSubscriptionDto) dto;

        return CartSubscriptionDocument.builder()
                .customerId(subscriptionDto.getCustomerId())
                .subscriptionItems(toMongoSubscriptionItems(subscriptionDto.getSubscriptionItems()))
                .skuId(subscriptionDto.getSkuId())
                .orderId(subscriptionDto.getOrderId())
                .consignmentId(subscriptionDto.getConsignmentId())
                .transportShipmentId(subscriptionDto.getTransportShipmentId())
                .shipmentGroupId(subscriptionDto.getShipmentGroupId())
                .calendarUnit(subscriptionDto.getCalendarUnit())
                .subscriptionStartDate(subscriptionDto.getSubscriptionStartDate())
                .currentSubscriptionCycleStatus(subscriptionDto.getCurrentSubscriptionCycleStatus())
                .fulfilledCycleCounts(subscriptionDto.getRemainingCycleCounts())
                .remainingCycleCounts(subscriptionDto.getRemainingCycleCounts())
                .amountPaid(subscriptionDto.getAmountPaid())
                .customerAddressId(subscriptionDto.getCustomerAddressId())
                .communityId(subscriptionDto.getCommunityId())
                .build();
    }

    private void updateMongoDocument(CartSubscriptionDocument document, Object dto) {
        if (!(dto instanceof CartSubscriptionDto)) {
            throw new IllegalArgumentException("Invalid DTO type provided to converter.");
        }
        CartSubscriptionDto subscriptionDto = (CartSubscriptionDto) dto;

        document.setCustomerId(subscriptionDto.getCustomerId());
        document.setSubscriptionItems(toMongoSubscriptionItems(subscriptionDto.getSubscriptionItems()));
        document.setSkuId(subscriptionDto.getSkuId());
        document.setOrderId(subscriptionDto.getOrderId());
        document.setConsignmentId(subscriptionDto.getConsignmentId());
        document.setTransportShipmentId(subscriptionDto.getTransportShipmentId());
        document.setShipmentGroupId(subscriptionDto.getShipmentGroupId());
        document.setCalendarUnit(subscriptionDto.getCalendarUnit());
        document.setSubscriptionStartDate(subscriptionDto.getSubscriptionStartDate());
        document.setCurrentSubscriptionCycleStatus(subscriptionDto.getCurrentSubscriptionCycleStatus());
        document.setFulfilledCycleCounts(subscriptionDto.getFulfilledCycleCounts());
        document.setRemainingCycleCounts(subscriptionDto.getRemainingCycleCounts());
        document.setAmountPaid(subscriptionDto.getAmountPaid());
        document.setCustomerAddressId(subscriptionDto.getCustomerAddressId());
        document.setCommunityId(subscriptionDto.getCommunityId());
    }

    private List<CartSubscriptionDocument.SubscriptionItem> toMongoSubscriptionItems(List<CartSubscriptionDto.SubscriptionItem> dtoItems) {
        if (dtoItems == null) return null;
        return dtoItems.stream()
                .map(dtoItem -> CartSubscriptionDocument.SubscriptionItem.builder()
                        .productId(dtoItem.getProductId())
                        .variantId(dtoItem.getVariantId())
                        .quantity(dtoItem.getQuantity())
                        .sellerId(dtoItem.getSellerId())
                        .build())
                .collect(Collectors.toList());
    }
}
