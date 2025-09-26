package com.bhagwat.retail.cart.entity;
import com.bhagwat.retail.cart.enums.CalendarUnit;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Command-side entity for PostgreSQL, representing the source of truth.
 * This entity enforces data integrity and unique constraints.
 */
@Entity
@Table(name = "cart_subscription", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "community_id", "calendar_unit"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "customer_id")
    private String customerId;

    private String sellerId;

    @ElementCollection
    @CollectionTable(name = "product_variants", joinColumns = @JoinColumn(name = "subscription_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "product_variant_id")
    private Map<String, String> productVariantId;

    private String skuId;
    private String orderId;
    private String consignmentId;
    private String transportShipmentId;
    private String shipmentGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_unit")
    private CalendarUnit calendarUnit;

    private LocalDate subscriptionStartDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionCycleStatus currentSubscriptionCycleStatus;

    private Integer fulfilledCycleCounts;
    private Integer remainingCycleCounts;
    private BigDecimal amountPaid;

    private String customerAddressId;

    @Column(name = "community_id")
    private String communityId;
}