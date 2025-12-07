package com.high.order.domain.repository;

import com.high.order.domain.entity.OrderItem;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository {
    Optional<OrderItem> findByOrderItemIdAndDeletedAtIsNull(UUID orderItemId);

    Optional<OrderItem> findByOrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(UUID orderId, UUID orderItemId, UUID producerId);

    Optional<OrderItem> findByOrderIdAndOrderItemIdAndDeletedAtIsNull(UUID orderId,
        UUID orderItemId);

    OrderItem save(OrderItem orderItem);
}
