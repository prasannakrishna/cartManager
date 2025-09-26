package com.bhagwat.retail.cart.repository;

import com.bhagwat.retail.cart.entity.CartSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartSubscriptionRepository extends JpaRepository<CartSubscription, String> {
}
