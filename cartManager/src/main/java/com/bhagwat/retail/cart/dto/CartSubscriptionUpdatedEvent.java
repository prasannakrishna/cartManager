package com.bhagwat.retail.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an existing CartSubscription is updated.
 * This event carries the updated data to be consumed by the query service
 * to update the read-model database (MongoDB).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionUpdatedEvent {
    private String id;
    private CartSubscriptionDto subscriptionDto;
}