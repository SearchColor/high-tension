package com.high.coupon.application.provider;

import com.high.coupon.infrastructure.client.dto.OrderResponse;
import java.util.UUID;

/**
 * 외부 주문 정보 가져올 때 의존 인터페이스
 */
public interface OrderProvider {
    OrderResponse getOrder(UUID orderId);
}
