package com.bhagwat.retail.cart.repository;

import com.bhagwat.retail.cart.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents();

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findRetryableFailedEvents();
}
