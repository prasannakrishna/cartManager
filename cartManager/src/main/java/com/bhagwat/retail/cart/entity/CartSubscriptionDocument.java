package com.bhagwat.retail.cart.entity;

import com.bhagwat.retail.cart.enums.CalendarUnit;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Query-side document for MongoDB, optimized for fast retrieval.
 */
/**
 * MongoDB Document for Cart Subscription, used for the Query/Read side.
 * This document is updated to support the new 'subscriptionItems' list.
 */
@Document(collection = "cart_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionDocument {

    @Id
    private String id;

    private String customerId;

    // CRITICAL UPDATE: Replaced 'sellerId' and 'productVariantId' with the item list
    @Field("subscription_items")
    private List<SubscriptionItem> subscriptionItems;

    private String skuId;
    private String orderId;
    private String consignmentId;
    private String transportShipmentId;
    private String shipmentGroupId;
    private CalendarUnit calendarUnit;
    private LocalDate subscriptionStartDate;
    private SubscriptionCycleStatus currentSubscriptionCycleStatus;
    private Integer fulfilledCycleCounts;
    private Integer remainingCycleCounts;
    private BigDecimal amountPaid;
    private String customerAddressId;
    private String communityId;

    /**
     * Inner class representing a single subscribed line item.
     * This ensures the seller ID is associated with the product variant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionItem {
        @Field("product_id")
        private String productId;

        @Field("variant_id")
        private String variantId;

        private Integer quantity;

        @Field("seller_id")
        private String sellerId;
    }
}