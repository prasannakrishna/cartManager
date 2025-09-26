package com.bhagwat.retail.cart.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a CartSubscription is deleted.
 * This event carries the ID of the deleted subscription to be consumed
 * by the query service to remove the corresponding document from MongoDB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionDeletedEvent {
    private String id;
}
