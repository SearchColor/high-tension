package com.high.order.application.dto.request;

import com.high.order.domain.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusChangeRequest(
    @NotNull(message = "변경할 상태 입력은 필수입니다.")
    OrderStatus orderStatus
) {

}
