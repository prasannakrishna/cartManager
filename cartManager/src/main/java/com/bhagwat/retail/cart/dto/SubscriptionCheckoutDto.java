package com.bhagwat.retail.cart.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCheckoutDto {
    private String communityId;
    private List<ProductSummary> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private String productId;
        private Long totalCustomers;
        private Long totalQuantity;
        private List<CustomerSummary> customers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private String customerId;
        private Long subscriptionQuantity;
        private Map<String, String> productVariantId; // Map<productId, variantId>
    }
}