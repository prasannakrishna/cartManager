package com.bhagwat.retail.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityOrderCreatedEvent {
    private String eventType = "COMMUNITY_ORDER_CREATED";
    private String communityOrderId;
    private String communityId;
    private String calendarUnit;
    private List<CommunityOrderItemDto> items;
    private BigDecimal totalAmount;
    private String currency;
    private String checkoutDate;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityOrderItemDto {
        private String productId;
        private String variantId;
        private String sellerId;
        private Integer totalQuantity;
        private BigDecimal pricePerUnit;
    }
}
