package com.high.order.domain.repository;

import com.high.order.domain.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findAll();
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
    List<Order> findAllByDeletedAtIsNull();
    Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId);
    List<Order> findOrdersForSeller(UUID sellerId);
    List<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId);
    Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId);

}
