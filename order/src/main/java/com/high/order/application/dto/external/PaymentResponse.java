package com.high.order.application.dto.external;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID orderId,
    UUID userId,
    BigDecimal amount,
    String status
) {

}
