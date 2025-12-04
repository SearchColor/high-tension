package com.high.orchestration.domain.vo;

import lombok.Getter;

@Getter
public enum SagaType {

    ORDER_CREATE("주문 생성"),
    ORDER_CANCEL("주문 취소"),
    ORDER_REFUND("환불");

    private final String description;


    SagaType(String description) {
        this.description = description;
    }


}
