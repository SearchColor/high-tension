package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId);


    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE oi.producerId = :sellerId
""")
    Page<Order> findOrdersForSeller(UUID sellerId, Pageable pageable);


    Page<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE oi.producerId = :sellerId
    AND o.orderId = :orderId
""")
    Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId);
}
