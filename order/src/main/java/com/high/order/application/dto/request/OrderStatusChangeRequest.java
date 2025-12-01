package com.high.order.application.dto.request;

import com.high.order.domain.vo.OrderStatus;

public record OrderStatusChangeRequest(
    OrderStatus orderStatus
) {

}
