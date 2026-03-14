package com.bhagwat.retail.cart.dto;
import com.bhagwat.retail.cart.enums.CalendarUnit;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for CartSubscription. Used to expose a simplified and
 * tailored view of the entity to the client, preventing direct exposure of
 * the internal data model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionDto {

    private String id;
    private String customerId;

    // CRITICAL UPDATE: Replaced sellerId and productVariantId with this list
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
     * Inner class for a line item within the subscription.
     * Each item now includes the associated sellerId.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionItem {
        private String productId;
        private String variantId;
        private Integer quantity;

        // ADDED: sellerId is now part of the line item
        private String sellerId;
    }
}