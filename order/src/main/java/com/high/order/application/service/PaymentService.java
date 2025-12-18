package com.high.order.application.service;

import com.high.order.application.dto.external.PaymentResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;

public interface PaymentService {

    PaymentResponse getPaymentByOrderId(
        @PathVariable UUID orderId
    );
}
