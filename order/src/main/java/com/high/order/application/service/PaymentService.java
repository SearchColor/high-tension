package com.high.order.application.service;

import com.high.order.application.dto.external.PaymentResponse;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface PaymentService {

    //TODO: 주문 id로 결제 내역 단건 조회
    ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
        @PathVariable UUID paymentId
    );
}
