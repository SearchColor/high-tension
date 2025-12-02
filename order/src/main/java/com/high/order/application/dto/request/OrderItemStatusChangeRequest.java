package com.high.order.application.dto.request;

import com.high.order.domain.vo.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record OrderItemStatusChangeRequest(
    @NotNull
    OrderItemStatus orderItemStatus

) {

}
