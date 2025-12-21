package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.Order;
import com.high.order.domain.repository.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<Order> findAll(Pageable pageable) {
        return jpaOrderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId) {
        return jpaOrderRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public Page<Order> findAllByDeletedAtIsNull(Pageable pageable) {
        return jpaOrderRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Optional<Order> findByOrderIdAndCustomerIdAndDeletedAtIsNull(UUID orderId, UUID customerId) {
        return jpaOrderRepository.findByOrderIdAndCustomerIdAndDeletedAtIsNull(orderId, customerId);
    }

    @Override
    public Page<Order> findOrdersForSeller(UUID sellerId, Pageable pageable) {
        return jpaOrderRepository.findOrdersForSeller(sellerId, pageable);
    }

    @Override
    public Page<Order> findAllByCustomerIdAndDeletedAtIsNull(UUID userId, Pageable pageable) {
        return jpaOrderRepository.findAllByCustomerIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public Optional<Order> findOrderForSeller(UUID orderId, UUID sellerId) {
        return jpaOrderRepository.findOrderForSeller(orderId, sellerId);
    }


}
