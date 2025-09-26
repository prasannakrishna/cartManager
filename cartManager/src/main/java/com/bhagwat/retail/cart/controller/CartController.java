package com.bhagwat.retail.cart.controller;
import com.bhagwat.retail.cart.dto.AddToCartRequest;
import com.bhagwat.retail.cart.dto.CartResponse;
import com.bhagwat.retail.cart.exception.CartNotFoundException;
import com.bhagwat.retail.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(request);
        // invoke api to product service, get appropriate inventory key
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String customerId) {
        try {
            CartResponse response = cartService.getCartByCustomerId(customerId);
            return ResponseEntity.ok(response);
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/checkout/{customerId}")
    public ResponseEntity<CartResponse> releaseCart(@PathVariable String customerId) {
        try {
            CartResponse response = cartService.releaseCart(customerId);
            return ResponseEntity.ok(response);
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Global exception handler for IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}