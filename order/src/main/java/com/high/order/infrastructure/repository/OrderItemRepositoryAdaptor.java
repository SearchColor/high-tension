package com.high.order.infrastructure.repository;

import com.high.order.domain.entity.OrderItem;
import com.high.order.domain.repository.OrderItemRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemRepositoryAdaptor implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;

    @Override
    public Optional<OrderItem> findByOrderItemIdAndDeletedAtIsNull(UUID orderItemId) {
        return jpaOrderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId);
    }
}
