package com.bhagwat.retail.cart.entity;

import com.bhagwat.retail.cart.enums.CalendarUnit;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Query-side document for MongoDB, optimized for fast retrieval.
 */
@Document(collection = "cart_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscriptionDocument {

    @Id
    private String id;

    private String customerId;
    private String sellerId;

    // Mongo can directly store Map<String, String> without complex annotations
    @Field("product_variant_id")
    private Map<String, String> productVariantId;

    private String skuId;
    private String orderId;
    private String consignmentId;
    private String transportShipmentId;
    private String shipmentGroupId;
    private CalendarUnit calendarUnit;
    private LocalDate subscriptionStartDate;
    private SubscriptionCycleStatus currentSubscriptionCycleStatus;
    private Integer fulfilledCycleCounts;
    private Integer remainingCycleCounts;
    private BigDecimal amountPaid;
    private String customerAddressId;
    private String communityId;
}

