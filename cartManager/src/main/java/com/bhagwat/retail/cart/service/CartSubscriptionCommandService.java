package com.bhagwat.retail.cart.service;

import com.bhagwat.retail.cart.dto.CartSubscriptionCreatedEvent;
import com.bhagwat.retail.cart.dto.CartSubscriptionDeletedEvent;
import com.bhagwat.retail.cart.dto.CartSubscriptionDto;
import com.bhagwat.retail.cart.dto.CartSubscriptionUpdatedEvent;
import com.bhagwat.retail.cart.entity.CartSubscription;
import com.bhagwat.retail.cart.repository.CartSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartSubscriptionCommandService {
    private final CartSubscriptionRepository postgresRepository;
    private final CartSubscriptionEventProducer kafkaProducerService;

    @Transactional
    public CartSubscriptionDto createSubscription(CartSubscriptionDto dto) {
        CartSubscription entity = toPostgresEntity(dto);
        CartSubscription savedEntity = postgresRepository.save(entity);
        CartSubscriptionDto createdDto = toDto(savedEntity);

        CartSubscriptionCreatedEvent event = new CartSubscriptionCreatedEvent(savedEntity.getId(), toDto(savedEntity));
        kafkaProducerService.send("cart-subscriptions-topic", event);

        return createdDto;
    }

    @Transactional
    public CartSubscriptionDto updateSubscription(String id, CartSubscriptionDto dto) {
        return postgresRepository.findById(id).map(existing -> {
            // Update fields from DTO
            existing.setSellerId(dto.getSellerId());
            existing.setProductVariantId(dto.getProductVariantId());
            existing.setSkuId(dto.getSkuId());
            existing.setOrderId(dto.getOrderId());
            existing.setConsignmentId(dto.getConsignmentId());
            existing.setTransportShipmentId(dto.getTransportShipmentId());
            existing.setShipmentGroupId(dto.getShipmentGroupId());
            existing.setCalendarUnit(dto.getCalendarUnit());
            existing.setSubscriptionStartDate(dto.getSubscriptionStartDate());
            existing.setCurrentSubscriptionCycleStatus(dto.getCurrentSubscriptionCycleStatus());
            existing.setFulfilledCycleCounts(dto.getFulfilledCycleCounts());
            existing.setRemainingCycleCounts(dto.getRemainingCycleCounts());
            existing.setAmountPaid(dto.getAmountPaid());
            existing.setCustomerAddressId(dto.getCustomerAddressId());

            CartSubscription updatedEntity = postgresRepository.save(existing);
            CartSubscriptionDto updatedDto = toDto(updatedEntity);

            CartSubscriptionUpdatedEvent event = new CartSubscriptionUpdatedEvent(updatedEntity.getId(), toDto(updatedEntity));
            kafkaProducerService.send("cart-subscriptions-topic", event);

            return updatedDto;
        }).orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + id));
    }

    @Transactional
    public void deleteSubscription(String id) {
        if (!postgresRepository.existsById(id)) {
            throw new RuntimeException("Subscription not found with ID: " + id);
        }
        postgresRepository.deleteById(id);
        // Publish event to Kafka for the query side to consume
        CartSubscriptionDeletedEvent event = new CartSubscriptionDeletedEvent(id);
        kafkaProducerService.send("cart-subscriptions-topic", event);
    }

    private CartSubscription toPostgresEntity(CartSubscriptionDto dto) {
        return CartSubscription.builder()
                .id(dto.getId())
                .customerId(dto.getCustomerId())
                .sellerId(dto.getSellerId())
                .productVariantId(dto.getProductVariantId())
                .skuId(dto.getSkuId())
                .orderId(dto.getOrderId())
                .consignmentId(dto.getConsignmentId())
                .transportShipmentId(dto.getTransportShipmentId())
                .shipmentGroupId(dto.getShipmentGroupId())
                .calendarUnit(dto.getCalendarUnit())
                .subscriptionStartDate(dto.getSubscriptionStartDate())
                .currentSubscriptionCycleStatus(dto.getCurrentSubscriptionCycleStatus())
                .fulfilledCycleCounts(dto.getFulfilledCycleCounts())
                .remainingCycleCounts(dto.getRemainingCycleCounts())
                .amountPaid(dto.getAmountPaid())
                .customerAddressId(dto.getCustomerAddressId())
                .communityId(dto.getCommunityId())
                .build();
    }

    private CartSubscriptionDto toDto(CartSubscription entity) {
        return CartSubscriptionDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .sellerId(entity.getSellerId())
                .productVariantId(entity.getProductVariantId())
                .skuId(entity.getSkuId())
                .orderId(entity.getOrderId())
                .consignmentId(entity.getConsignmentId())
                .transportShipmentId(entity.getTransportShipmentId())
                .shipmentGroupId(entity.getShipmentGroupId())
                .calendarUnit(entity.getCalendarUnit())
                .subscriptionStartDate(entity.getSubscriptionStartDate())
                .currentSubscriptionCycleStatus(entity.getCurrentSubscriptionCycleStatus())
                .fulfilledCycleCounts(entity.getFulfilledCycleCounts())
                .remainingCycleCounts(entity.getRemainingCycleCounts())
                .amountPaid(entity.getAmountPaid())
                .customerAddressId(entity.getCustomerAddressId())
                .communityId(entity.getCommunityId())
                .build();
    }
}
