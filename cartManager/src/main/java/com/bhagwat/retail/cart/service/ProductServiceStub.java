package com.bhagwat.retail.cart.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceStub {

    private final Map<String, BigDecimal> productPrices = new HashMap<>();

    public ProductServiceStub() {
        // Initialize with some dummy product prices
        productPrices.put("prod001", new BigDecimal("10.50"));
        productPrices.put("prod002", new BigDecimal("25.00"));
        productPrices.put("prod003", new BigDecimal("5.99"));
        productPrices.put("prod004", new BigDecimal("100.00"));
    }

    public Optional<BigDecimal> getProductPrice(String productId) {
        return Optional.ofNullable(productPrices.get(productId));
    }
}