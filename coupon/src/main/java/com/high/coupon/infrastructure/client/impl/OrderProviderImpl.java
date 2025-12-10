package com.high.coupon.infrastructure.client.impl;

import com.high.coupon.application.provider.OrderProvider;
import com.high.coupon.application.provider.dto.OrderInfo;
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
    public OrderInfo getOrder(UUID orderId){
        // Feign 데이터 받아옴
        OrderResponse infraDto = orderClient.getOrder(orderId).data();

        // 서비스가 쓸 데이터 매핑
        return new OrderInfo(
                infraDto.orderId(),
                infraDto.userId(),
                infraDto.couponIssueId()
        );
    }
}
