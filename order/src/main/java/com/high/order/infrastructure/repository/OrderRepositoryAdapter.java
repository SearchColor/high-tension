package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.Order;
import com.high.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaOrderRepository.findById(orderId);
    }

    @Override
    public List<Order> findAll() {
        return jpaOrderRepository.findAll();
    }

    @Override
    public Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId) {
        return jpaOrderRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public List<Order> findAllByDeletedAtIsNull() {
        return jpaOrderRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId) {
        return jpaOrderRepository.findByOrderIdAndCustomerIdAndDeletedAtIsNull(orderId, customerId);
    }

    @Override
    public List<Order> findOrdersForSeller(UUID sellerId) {
        return jpaOrderRepository.findOrdersForSeller(sellerId);
    }

    @Override
    public List<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId) {
        return jpaOrderRepository.findAllByCustomerIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId) {
        return jpaOrderRepository.findOrderForSeller(orderId, sellerId);
    }


}
