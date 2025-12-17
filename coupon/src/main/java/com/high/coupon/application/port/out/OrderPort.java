package com.high.coupon.application.port.out;

import com.high.coupon.application.port.out.dto.OrderInfo;
import java.util.UUID;

/**
 * 외부 주문 정보 가져올 때 의존 인터페이스
 */
public interface OrderPort {
    OrderInfo getOrder(UUID orderId);
}
