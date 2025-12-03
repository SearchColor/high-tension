package com.high.orchestration.application.dto.internal.request;

import com.high.orchestration.application.dto.request.OrderCreateRequest;
import com.high.orchestration.application.dto.request.OrderItemRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

public record InternalOrderCreateRequest(
    UUID couponId,
    UUID sagaId,
    UUID orderId,

    @NotBlank(message = "수령자 정보는 필수입니다")
    String recipient,

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
        regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
        message = "전화번호 형식은 010-XXXX-XXXX 입니다."
    )
    String recipientContact,

    @NotBlank(message = "주소는 필수입니다")
    String deliveryAddress,

    String detailAddress,

    String requestMessage,

    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    List<OrderItemRequest> itemList
) {
    public static InternalOrderCreateRequest from(UUID orderId, UUID sagaId, OrderCreateRequest orderCreateRequest) {
        return new InternalOrderCreateRequest(
            orderCreateRequest.couponId(),
            sagaId,
            orderId,
            orderCreateRequest.recipient(),
            orderCreateRequest.recipientContact(),
            orderCreateRequest.deliveryAddress(),
            orderCreateRequest.detailAddress(),
            orderCreateRequest.requestMessage(),
            orderCreateRequest.itemList()
        );
    }
}
