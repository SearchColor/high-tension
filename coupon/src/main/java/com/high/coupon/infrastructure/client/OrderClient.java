package com.high.coupon.infrastructure.client;

import com.high.coupon.infrastructure.client.dto.OrderResponse;
import com.high.coupon.infrastructure.config.FeignConfig;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * order <-> coupon 통신 (saga-orchestration)
 * orderId 내 couponIssueId 가져와 완료 처리를 위함
 */
@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    ApiResponse<OrderResponse> getOrder(@PathVariable UUID orderId);
}
