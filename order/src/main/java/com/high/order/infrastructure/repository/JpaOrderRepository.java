package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    List<Order> findAllByDeletedAtIsNull();

    Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE oi.producerId = :sellerId
""")
    List<Order> findOrdersForSeller(UUID sellerId);


    List<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE oi.producerId = :sellerId
    AND o.orderId = :orderId
""")
    Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId);
}
