package com.bhagwat.retail.cart.service;

import com.bhagwat.retail.cart.dto.AddToCartRequest;
import com.bhagwat.retail.cart.dto.CartItemDto;
import com.bhagwat.retail.cart.dto.CartResponse;
import com.bhagwat.retail.cart.dto.CustomerOrderCreatedEvent;
import com.bhagwat.retail.cart.entity.Cart;
import com.bhagwat.retail.cart.entity.CartItem;
import com.bhagwat.retail.cart.entity.OutboxEvent;
import com.bhagwat.retail.cart.enums.CartStatus;
import com.bhagwat.retail.cart.exception.CartNotFoundException;
import com.bhagwat.retail.cart.repository.CartItemRepository;
import com.bhagwat.retail.cart.repository.CartRepository;
import com.bhagwat.retail.cart.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceStub productServiceStub;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private static final String CUSTOMER_ORDER_TOPIC = "customer-order-events";

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductServiceStub productServiceStub,
                       OutboxEventRepository outboxEventRepository,
                       ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceStub = productServiceStub;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(request.getCustomerId(), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCartId(UUID.randomUUID().toString());
                    newCart.setCustomerId(request.getCustomerId());
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });

        BigDecimal pricePerUnit = productServiceStub.getProductPrice(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));

        Optional<CartItem> existingItem = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), request.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setTotalItemPrice(pricePerUnit.multiply(new BigDecimal(cartItem.getQuantity())));
        } else {
            cartItem = new CartItem();
            cartItem.setCartItemId(UUID.randomUUID().toString());
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPricePerUnit(pricePerUnit);
            cartItem.setTotalItemPrice(pricePerUnit.multiply(new BigDecimal(request.getQuantity())));
            cart.addCartItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        return mapCartToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByCustomerId(String customerId) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for customer: " + customerId));
        return mapCartToResponse(cart);
    }

    /**
     * Checkout: marks cart INACTIVE and writes CustomerOrderCreatedEvent to the outbox table
     * in the same DB transaction. The OutboxRelayService picks up the events and publishes
     * them to Kafka asynchronously — preventing the dual-write problem.
     */
    @Transactional
    public CartResponse releaseCart(String customerId) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for customer: " + customerId));

        cart.setStatus(CartStatus.INACTIVE);
        cartRepository.save(cart);

        // Write events to outbox in the SAME transaction as the cart status change.
        // This guarantees atomicity: either both the cart update AND the event records
        // are committed together, or neither is — no partial state.
        for (CartItem item : cart.getItems()) {
            BigDecimal shippingCost = BigDecimal.valueOf(5.99);
            BigDecimal taxAmount = item.getTotalItemPrice().multiply(BigDecimal.valueOf(0.08));

            CustomerOrderCreatedEvent event = new CustomerOrderCreatedEvent(
                    "CUSTOMER_ORDER_CREATED",
                    UUID.randomUUID().toString(),
                    customerId,
                    item.getProductId(),
                    item.getInventoryKey(),
                    item.getQuantity(),
                    item.getPricePerUnit(),
                    item.getTotalItemPrice(),
                    shippingCost,
                    taxAmount,
                    "USD",
                    null,
                    null,
                    item.getInventoryKey(),
                    Instant.now()
            );

            try {
                OutboxEvent outbox = OutboxEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .topic(CUSTOMER_ORDER_TOPIC)
                        .messageKey(customerId)
                        .eventType("com.bhagwat.retail.cart.dto.CustomerOrderCreatedEvent")
                        .payload(objectMapper.writeValueAsString(event))
                        .build();
                outboxEventRepository.save(outbox);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize CustomerOrderCreatedEvent for outbox", e);
            }
        }

        log.info("Cart {} checked out; {} outbox events written for customer {}", cart.getCartId(), cart.getItems().size(), customerId);
        return mapCartToResponse(cart);
    }

    private CartResponse mapCartToResponse(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(item -> new CartItemDto(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPricePerUnit(),
                        item.getTotalItemPrice()
                ))
                .collect(Collectors.toList());

        BigDecimal totalCartPrice = itemDtos.stream()
                .map(CartItemDto::getTotalItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getCartId(),
                cart.getCustomerId(),
                cart.getStatus(),
                itemDtos,
                totalCartPrice
        );
    }
}
