package com.bhagwat.retail.cart.service;

import com.bhagwat.retail.cart.dto.AddToCartRequest;
import com.bhagwat.retail.cart.dto.CartItemDto;
import com.bhagwat.retail.cart.dto.CartResponse;
import com.bhagwat.retail.cart.dto.CustomerOrderCreatedEvent;
import com.bhagwat.retail.cart.entity.Cart;
import com.bhagwat.retail.cart.entity.CartItem;
import com.bhagwat.retail.cart.enums.CartStatus;
import com.bhagwat.retail.cart.exception.CartNotFoundException;
import com.bhagwat.retail.cart.repository.CartItemRepository;
import com.bhagwat.retail.cart.repository.CartRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceStub productServiceStub;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CUSTOMER_ORDER_TOPIC = "customer-order-events";

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductServiceStub productServiceStub,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceStub = productServiceStub;
        this.kafkaTemplate = kafkaTemplate;
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
     * Checkout: marks cart INACTIVE and publishes a CustomerOrderCreatedEvent
     * to Kafka for each item in the cart. orderService picks these up.
     */
    @Transactional
    public CartResponse releaseCart(String customerId) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for customer: " + customerId));

        cart.setStatus(CartStatus.INACTIVE);
        cartRepository.save(cart);

        // Publish one CustomerOrderCreatedEvent per cart item
        for (CartItem item : cart.getItems()) {
            BigDecimal shippingCost = BigDecimal.valueOf(5.99);
            BigDecimal taxAmount = item.getTotalItemPrice().multiply(BigDecimal.valueOf(0.08));

            CustomerOrderCreatedEvent event = new CustomerOrderCreatedEvent(
                    "CUSTOMER_ORDER_CREATED",
                    UUID.randomUUID().toString(),   // orderId
                    customerId,
                    item.getProductId(),
                    item.getInventoryKey(),          // variantId (using inventoryKey as proxy until variant wired in)
                    item.getQuantity(),
                    item.getPricePerUnit(),
                    item.getTotalItemPrice(),
                    shippingCost,
                    taxAmount,
                    "USD",
                    null,  // communityId — not known at cart level for plain orders
                    null,  // sellerId — to be enriched by orderService
                    item.getInventoryKey(),
                    Instant.now()
            );
            kafkaTemplate.send(CUSTOMER_ORDER_TOPIC, customerId, event);
        }

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
