package com.high.order.application.dto.external;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID orderId,
    UUID paymentId,
    BigDecimal amount,
    String status,
    String pgTid,
    LocalDateTime createdAt
) {

}
