package com.bhagwat.retail.cart.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionCreatedEvent {
    private String id;
    private CartSubscriptionDto subscriptionDto;
}