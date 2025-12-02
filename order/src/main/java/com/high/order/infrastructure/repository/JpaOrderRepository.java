package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    List<Order> findAllByDeletedAtIsNull();
}
