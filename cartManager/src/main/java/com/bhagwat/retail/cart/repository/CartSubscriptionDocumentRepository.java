package com.bhagwat.retail.cart.repository;

import com.bhagwat.retail.cart.entity.CartSubscriptionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartSubscriptionDocumentRepository extends MongoRepository<CartSubscriptionDocument, String> {
}
