package com.high.product.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.library.module.response.ApiResponse;

public interface OrderService {
	ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderdetail(
		@PathVariable("orderId") UUID orderId);
}
