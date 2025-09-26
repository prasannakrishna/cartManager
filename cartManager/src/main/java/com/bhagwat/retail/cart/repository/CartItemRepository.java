package com.bhagwat.retail.cart.repository;

import com.bhagwat.retail.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    Optional<CartItem> findByCartCartIdAndProductId(String cartId, String productId);
}