package com.bhagwat.retail.cart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable class to represent a single product line item within a subscription.
 * This accommodates product ID, variant ID, quantity, and the owning seller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SubscriptionItem {
    @Column(nullable = false)
    private String productId;
    @Column(nullable = false)
    private String variantId;
    @Column(nullable = false)
    private Integer quantity; // The quantity of this specific product variant

    // ADDED: sellerId is now part of the line item
    @Column(nullable = false)
    private String sellerId;
}
