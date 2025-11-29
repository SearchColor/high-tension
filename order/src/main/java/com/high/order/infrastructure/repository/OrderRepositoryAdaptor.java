package com.high.order.infrastructure.repository;

import com.high.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdaptor implements OrderRepository {
    private final JpaOrderRepository jpaOrderRepository;
}
