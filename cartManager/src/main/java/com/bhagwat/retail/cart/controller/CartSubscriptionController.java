package com.bhagwat.retail.cart.controller;
import com.bhagwat.retail.cart.dto.CartSubscriptionDto;
import com.bhagwat.retail.cart.dto.CommunityOrderCreatedEvent;
import com.bhagwat.retail.cart.dto.ProspectusDto;
import com.bhagwat.retail.cart.dto.SubscriptionCheckoutDto;
import com.bhagwat.retail.cart.service.CartSubscriptionCommandService;
import com.bhagwat.retail.cart.service.CartSubscriptionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing CartSubscription resources.
 * This controller adheres to the CQRS pattern.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class CartSubscriptionController {

    private final CartSubscriptionCommandService commandService;
    private final CartSubscriptionQueryService queryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String COMMUNITY_ORDER_TOPIC = "community-order-events";

    /**
     * Creates a new cart subscription. This is a command operation.
     */
    @PostMapping
    public ResponseEntity<CartSubscriptionDto> createSubscription(@RequestBody CartSubscriptionDto dto) {
        CartSubscriptionDto created = commandService.createSubscription(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieves a cart subscription by its ID. This is a query operation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CartSubscriptionDto> getSubscriptionById(@PathVariable String id) {
        return queryService.getSubscriptionById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Updates an existing cart subscription. This is a command operation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CartSubscriptionDto> updateSubscription(@PathVariable String id, @RequestBody CartSubscriptionDto dto) {
        try {
            CartSubscriptionDto updated = commandService.updateSubscription(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a cart subscription by its ID. This is a command operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable String id) {
        try {
            commandService.deleteSubscription(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/checkout")
    public ResponseEntity<List<SubscriptionCheckoutDto>> getSubscriptionsForCheckout(
            @RequestParam("checkoutDate") LocalDate checkoutDate,
            @RequestParam(value = "groupByProductId", defaultValue = "false") boolean groupByProductId) {
        List<SubscriptionCheckoutDto> result = queryService.getSubscriptionsForCheckout(checkoutDate, groupByProductId);
        return ResponseEntity.ok(result);
    }

    /**
     * Triggers community order checkout:
     * Groups subscriptions by community and publishes a CommunityOrderCreatedEvent
     * to Kafka for each community. orderService picks these up and persists them.
     */
    @PostMapping("/checkout")
    public ResponseEntity<List<SubscriptionCheckoutDto>> checkoutSubscriptions(
            @RequestParam("checkoutDate") LocalDate checkoutDate,
            @RequestParam(value = "calendarUnit", defaultValue = "DAY") String calendarUnit) {

        List<SubscriptionCheckoutDto> checkouts = queryService.getSubscriptionsForCheckout(checkoutDate, false);

        checkouts.forEach(checkout -> {
            List<CommunityOrderCreatedEvent.CommunityOrderItemDto> items = checkout.getProducts().stream()
                    .map(p -> new CommunityOrderCreatedEvent.CommunityOrderItemDto(
                            p.getProductId(),
                            null,   // variantId — not in SubscriptionCheckoutDto
                            null,   // sellerId — not in SubscriptionCheckoutDto at this level
                            p.getTotalQuantity() != null ? p.getTotalQuantity().intValue() : 0,
                            BigDecimal.ZERO  // price enriched by orderService
                    )).collect(Collectors.toList());

            CommunityOrderCreatedEvent event = new CommunityOrderCreatedEvent(
                    "COMMUNITY_ORDER_CREATED",
                    UUID.randomUUID().toString(),
                    checkout.getCommunityId(),
                    calendarUnit,
                    items,
                    BigDecimal.ZERO,  // totalAmount enriched by orderService
                    "USD",
                    checkoutDate.toString(),
                    Instant.now()
            );
            kafkaTemplate.send(COMMUNITY_ORDER_TOPIC, checkout.getCommunityId(), event);
        });

        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/prospectus")
    public ResponseEntity<List<ProspectusDto>> getProspectus(
            @RequestParam("sellerId") String sellerId,
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam("timeframe") Integer timeframe) {

        if (timeframe <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ProspectusDto> result = queryService.getProspectus(sellerId, productId, timeframe);
        return ResponseEntity.ok(result);
    }
}
