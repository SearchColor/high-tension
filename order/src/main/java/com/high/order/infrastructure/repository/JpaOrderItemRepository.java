package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, UUID> {

}
