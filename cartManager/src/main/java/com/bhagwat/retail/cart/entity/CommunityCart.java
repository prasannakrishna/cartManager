package com.bhagwat.retail.cart.entity;

import com.bhagwat.retail.cart.enums.CalendarUnit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a community-level cart that aggregates subscription orders
 * from multiple customers within a community.
 *
 * Lifecycle: CREATED → IN_PROCESS → CLOSED → SHIPPED → IN_TRANSIT → DELIVERED
 * Cart closes based on checkout frequency (daily, weekly, bi-weekly, etc.)
 * and notifications are sent to sellers upon close.
 */
@Entity
@Table(name = "community_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCart {

    @Id
    private String communityCartId;

    @Column(name = "community_id", nullable = false)
    private String communityId;

    @Column(name = "seller_org_id", nullable = false)
    private String sellerOrgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_status", nullable = false)
    private CommunityCartStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkout_frequency", nullable = false)
    private CalendarUnit checkoutFrequency;

    @Column(name = "cart_close_date")
    private LocalDate cartCloseDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "target_order_value", precision = 19, scale = 2)
    private BigDecimal targetOrderValue;

    @Column(name = "total_cart_value", precision = 19, scale = 2)
    private BigDecimal totalCartValue;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson; // JSON list of seller-product entries

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (communityCartId == null) communityCartId = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = CommunityCartStatus.CREATED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CommunityCartStatus {
        CREATED, IN_PROCESS, CLOSED, SHIPPED, IN_TRANSIT, DELIVERED, ON_HOLD
    }
}
