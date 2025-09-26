package com.bhagwat.retail.cart.dto;
import com.bhagwat.retail.cart.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CartResponse {
    private String cartId;
    private String customerId;
    private CartStatus status;
    private List<CartItemDto> items;
    private BigDecimal totalCartPrice;
}