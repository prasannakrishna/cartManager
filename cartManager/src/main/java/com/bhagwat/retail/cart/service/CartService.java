package com.bhagwat.retail.cart.service;
import com.bhagwat.retail.cart.dto.AddToCartRequest;
import com.bhagwat.retail.cart.dto.CartItemDto;
import com.bhagwat.retail.cart.dto.CartResponse;
import com.bhagwat.retail.cart.entity.Cart;
import com.bhagwat.retail.cart.entity.CartItem;
import com.bhagwat.retail.cart.enums.CartStatus;
import com.bhagwat.retail.cart.exception.CartNotFoundException;
import com.bhagwat.retail.cart.repository.CartItemRepository;
import com.bhagwat.retail.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceStub productServiceStub;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductServiceStub productServiceStub) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceStub = productServiceStub;
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        // Find existing active cart or create a new one
        Cart cart = cartRepository.findByCustomerIdAndStatus(request.getCustomerId(), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCartId(UUID.randomUUID().toString());
                    newCart.setCustomerId(request.getCustomerId());
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });

        // Get product price from stub (simulating Product Service call)
        BigDecimal pricePerUnit = productServiceStub.getProductPrice(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));

        // Check if item already exists in cart
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
            cart.addCartItem(cartItem); // Add to cart's items collection
        }

        cartItemRepository.save(cartItem); // Save or update the cart item
        cartRepository.save(cart); // Persist changes to cart (updatedAt)

        return mapCartToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByCustomerId(String customerId) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for customer: " + customerId));
        return mapCartToResponse(cart);
    }

    @Transactional
    public CartResponse releaseCart(String customerId) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for customer: " + customerId));

        cart.setStatus(CartStatus.INACTIVE);
        cartRepository.save(cart);

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