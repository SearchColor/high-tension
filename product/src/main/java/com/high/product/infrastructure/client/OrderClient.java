package com.high.product.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.port.OrderQueryPort;
import com.library.module.response.ApiResponse;

@FeignClient(name = "order-service")
public interface OrderClient extends OrderQueryPort {

	@GetMapping("/api/v1/orders/{orderId}")
	ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderdetail(
		@PathVariable("orderId") UUID orderId);
}
