package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.OrderItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, UUID> {

    Optional<OrderItem> findByOrderItemIdAndDeletedAtIsNull(UUID orderItemId);

    Optional<OrderItem> findByOrder_OrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(UUID orderId, UUID orderItemId, UUID producerId);

    Optional<OrderItem> findByOrder_OrderIdAndOrderItemIdAndDeletedAtIsNull(UUID orderId, UUID orderItemId);

    @Query("""
    select oi
    from OrderItem oi
    join oi.order o
    where oi.orderItemId = :orderItemId
      and o.customerId = :customerId
      and oi.deletedAt is null
""")
    Optional<OrderItem> findOrderItemForUser(
        @Param("orderItemId") UUID orderItemId,
        @Param("customerId") UUID customerId
    );
}
