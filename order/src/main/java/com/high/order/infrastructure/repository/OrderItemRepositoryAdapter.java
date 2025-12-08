package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.OrderItem;
import com.high.order.domain.repository.OrderItemRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;

    @Override
    public Optional<OrderItem> findByOrderItemIdAndDeletedAtIsNull(UUID orderItemId) {
        return jpaOrderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId);
    }

    @Override
    public Optional<OrderItem> findByOrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(
        UUID orderId, UUID orderItemId, UUID producerId) {
        return jpaOrderItemRepository.findByOrder_OrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(orderId, orderItemId, producerId);
    }

    @Override
    public Optional<OrderItem> findByOrderIdAndOrderItemIdAndDeletedAtIsNull(UUID orderId,
        UUID orderItemId) {
        return jpaOrderItemRepository.findByOrder_OrderIdAndOrderItemIdAndDeletedAtIsNull(orderId, orderItemId);
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaOrderItemRepository.save(orderItem);
    }
}
