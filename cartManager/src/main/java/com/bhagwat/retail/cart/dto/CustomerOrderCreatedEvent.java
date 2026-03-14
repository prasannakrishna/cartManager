package com.bhagwat.retail.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderCreatedEvent {
    private String eventType = "CUSTOMER_ORDER_CREATED";
    private String orderId;
    private String customerId;
    private String productId;
    private String variantId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private String currency;
    private String communityId;
    private String sellerId;
    private String inventoryKey;
    private Instant createdAt;
}
