package com.bhagwat.retail.cart.service;

import com.bhagwat.retail.cart.dto.CartSubscriptionDto;
import com.bhagwat.retail.cart.dto.ProspectusDto;
import com.bhagwat.retail.cart.dto.SubscriptionCheckoutDto;
import com.bhagwat.retail.cart.entity.CartSubscriptionDocument;
import com.bhagwat.retail.cart.enums.SubscriptionCycleStatus;
import com.bhagwat.retail.cart.repository.CartSubscriptionDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Service for query (read) operations. It retrieves data from the MongoDB repository.
 */
@Service
@RequiredArgsConstructor
public class CartSubscriptionQueryService {

    private final CartSubscriptionDocumentRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public Optional<CartSubscriptionDto> getSubscriptionById(String id) {
        return mongoRepository.findById(id).map(this::toDto);
    }

    private CartSubscriptionDto toDto(CartSubscriptionDocument document) {
        return CartSubscriptionDto.builder()
                .id(document.getId())
                .customerId(document.getCustomerId())
                .sellerId(document.getSellerId())
                .productVariantId(document.getProductVariantId())
                .skuId(document.getSkuId())
                .orderId(document.getOrderId())
                .consignmentId(document.getConsignmentId())
                .transportShipmentId(document.getTransportShipmentId())
                .shipmentGroupId(document.getShipmentGroupId())
                .calendarUnit(document.getCalendarUnit())
                .subscriptionStartDate(document.getSubscriptionStartDate())
                .currentSubscriptionCycleStatus(document.getCurrentSubscriptionCycleStatus())
                .fulfilledCycleCounts(document.getFulfilledCycleCounts())
                .remainingCycleCounts(document.getRemainingCycleCounts())
                .amountPaid(document.getAmountPaid())
                .customerAddressId(document.getCustomerAddressId())
                .communityId(document.getCommunityId())
                .build();
    }

    public List<SubscriptionCheckoutDto> getSubscriptionsForCheckout(LocalDate checkoutDate, boolean groupByProductId) {
        Criteria dateCriteria = Criteria.where("subscriptionStartDate").lte(checkoutDate);
        Criteria statusCriteria = Criteria.where("currentSubscriptionCycleStatus").is(SubscriptionCycleStatus.ACTIVE);

        Criteria combinedCriteria = new Criteria().andOperator(dateCriteria, statusCriteria);

        Aggregation aggregation;
        if (groupByProductId) {
            aggregation = newAggregation(
                    match(combinedCriteria),
                    unwind("productVariantId"),
                    group("communityId", "productVariantId.key", "customerId")
                            .count().as("subscriptionQuantity")
                            .first("productVariantId.value").as("productVariantId"),
                    project("subscriptionQuantity", "productVariantId")
                            .and("communityId").previousOperation()
                            .and("productVariantId.key").as("productId")
                            .and("customerId").previousOperation(),
                    group("communityId", "productId")
                            .count().as("totalCustomers")
                            .sum("subscriptionQuantity").as("totalQuantity")
                            .push(new Document("customerId", "$customerId")
                                    .append("subscriptionQuantity", "$subscriptionQuantity")
                                    .append("productVariantId", "$productVariantId"))
                            .as("customers"),
                    project("totalCustomers", "totalQuantity", "customers")
                            .and("communityId").previousOperation()
                            .and("productId").previousOperation(),
                    group("communityId")
                            .push(new Document("productId", "$productId")
                                    .append("totalCustomers", "$totalCustomers")
                                    .append("totalQuantity", "$totalQuantity")
                                    .append("customers", "$customers"))
                            .as("products"),
                    project("products").and("communityId").previousOperation()
            );
        } else {
            aggregation = newAggregation(
                    match(combinedCriteria),
                    group("communityId").count().as("totalCustomers"),
                    project("totalCustomers").and("communityId").previousOperation()
            );
        }

        AggregationResults<SubscriptionCheckoutDto> results =
                mongoTemplate.aggregate(aggregation, "cart_subscriptions", SubscriptionCheckoutDto.class);
        return results.getMappedResults();
    }

    public List<ProspectusDto> getProspectus(String sellerId, String productId, Integer timeframe) {
        Criteria criteria = Criteria.where("sellerId").is(sellerId)
                .and("currentSubscriptionCycleStatus").is(SubscriptionCycleStatus.ACTIVE);

        if (productId != null && !productId.isEmpty()) {
            criteria.and("productVariantId." + productId).exists(true);
        }

        Aggregation aggregation = newAggregation(
                match(criteria),
                project("productVariantId", "remainingCycleCounts")
                        .andExpression("min({0}, {1})", timeframe, "$remainingCycleCounts").as("effectiveTimeframe"),
                unwind("productVariantId"),
                group("productVariantId.key")
                        .sum("effectiveTimeframe").as("totalEstimatedQuantity"),
                project("totalEstimatedQuantity").and("_id").as("productId")
        );

        AggregationResults<ProspectusDto> results =
                mongoTemplate.aggregate(aggregation, "cart_subscriptions", ProspectusDto.class);
        return results.getMappedResults();
    }
}
