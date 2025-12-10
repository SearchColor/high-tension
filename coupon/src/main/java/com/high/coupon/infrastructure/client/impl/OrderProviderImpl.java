package com.high.coupon.infrastructure.client.impl;

import com.high.coupon.application.provider.OrderProvider;
import com.high.coupon.infrastructure.client.OrderClient;
import com.high.coupon.infrastructure.client.dto.OrderResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 실제 feign 사용
 */
@Component
@RequiredArgsConstructor
public class OrderProviderImpl implements OrderProvider {

    private final OrderClient orderClient;

    @Override
    public OrderResponse getOrder(UUID orderId){
        return orderClient.getOrder(orderId).data();
    }
}
