package com.high.order.application.dto.request;

import com.high.order.domain.vo.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record OrderItemDeliveryStatusChangeRequest(
    @NotNull(message = "변경할 상태 입력은 필수입니다.")
    DeliveryStatus deliveryStatus
) {

}
