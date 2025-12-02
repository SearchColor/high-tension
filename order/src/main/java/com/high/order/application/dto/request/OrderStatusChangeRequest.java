package com.high.order.application.dto.request;

import com.high.order.domain.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusChangeRequest(
    @NotNull
    OrderStatus orderStatus
) {

}
