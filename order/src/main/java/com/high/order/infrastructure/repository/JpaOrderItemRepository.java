package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.OrderItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, UUID> {

    Optional<OrderItem> findByOrderItemIdAndDeletedAtIsNull(UUID orderItemId);

    Optional<OrderItem> findByOrder_OrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(UUID orderId, UUID orderItemId, UUID producerId);

    Optional<OrderItem> findByOrder_OrderIdAndOrderItemIdAndDeletedAtIsNull(UUID orderId, UUID orderItemId);
}
