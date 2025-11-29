package com.high.order.infrastructure.repository;

import com.high.order.domain.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemRepositoryAdaptor implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;
}
