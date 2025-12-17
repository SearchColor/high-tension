package com.high.product.application.port;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.library.module.response.ApiResponse;

public interface OrderQueryPort {
	ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
		@PathVariable("orderId") UUID orderId);
}
