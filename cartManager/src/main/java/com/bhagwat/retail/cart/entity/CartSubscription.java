package com.bhagwat.retail.cart.entity;
import com.bhagwat.retail.cart.enums.CalendarUnit;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    @CollectionTable(name = "subscription_item", joinColumns = @JoinColumn(name = "subscription_id"))
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "item_details", columnDefinition = "jsonb")
    private List<SubscriptionItem> subscriptionItems;

    private String skuId;
    private String orderId;
    private String consignmentId;
    private String transportShipmentId;
    private String shipmentGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_unit")
    private CalendarUnit calendarUnit;

    private LocalDate subscriptionStartDate;

    @Column(name = "cart_checkout_date")
    private LocalDate cartCheckOutDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionCycleStatus currentSubscriptionCycleStatus;

    private Integer fulfilledCycleCounts;
    private Integer remainingCycleCounts;
    private BigDecimal amountPaid;

    private String customerAddressId;

    @Column(name = "community_id")
    private String communityId;
}