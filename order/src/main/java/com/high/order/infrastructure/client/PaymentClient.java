package com.high.order.infrastructure.client;

import com.high.order.application.dto.external.PaymentResponse;
import com.high.order.application.service.PaymentService;
import com.high.order.infrastructure.config.feign.FeignConfig;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service", configuration = FeignConfig.class)
public interface PaymentClient extends PaymentService {

    //TODO: 주문 id로 결제 내역 단건 조회
    @GetMapping("/api/v1/payments/order/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable UUID orderId);
}
