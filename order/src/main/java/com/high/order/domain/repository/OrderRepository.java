package com.high.order.domain.repository;

import com.high.order.domain.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    Page<Order> findAll(Pageable pageable);
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId);
    Page<Order> findOrdersForSeller(UUID sellerId, Pageable pageable);
    Page<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
    Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId);

}
