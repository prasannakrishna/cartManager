package com.bhagwat.retail.cart.repository;

import com.bhagwat.retail.cart.entity.Cart;
import com.bhagwat.retail.cart.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByCustomerIdAndStatus(String customerId, CartStatus status);
}
