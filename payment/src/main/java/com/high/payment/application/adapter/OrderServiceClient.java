package com.high.payment.application.adapter;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.high.payment.application.dto.external.OrderDetailResponse;
import com.high.payment.infrastructure.config.FeignConfig;
import com.library.module.response.ApiResponse;

@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {
	@GetMapping("/api/v1/orders/{orderId}")
	ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable("orderId") UUID orderId);
}
