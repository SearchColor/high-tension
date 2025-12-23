package com.high.order.application.dto.event.request;

import java.util.List;
import java.util.UUID;

public record ProcessOrderSuccessCommand(
    UUID orderId,
    UUID userId,
    Integer paidAmount,
    String createdAt, //LocalDateTime으로 전송시 직렬화 오류남
    List<String> orderItemNameList
) {

    public static ProcessOrderSuccessCommand create(
        UUID orderId,
        UUID userId,
        Integer paidAmount,
        String createdAt,
        List<String> orderItemNameList
    ) {
        return new ProcessOrderSuccessCommand(orderId, userId, paidAmount, createdAt, orderItemNameList);
    }
}
