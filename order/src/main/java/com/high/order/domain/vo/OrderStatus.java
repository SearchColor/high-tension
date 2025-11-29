package com.high.order.domain.vo;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED("주문 생성"),
    CANCELED("주문 취소"),
    SUCCESS("주문 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
