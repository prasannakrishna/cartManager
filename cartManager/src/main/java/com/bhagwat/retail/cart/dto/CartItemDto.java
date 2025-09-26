package com.bhagwat.retail.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDto {
    private String productId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalItemPrice;
}