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

    public boolean isCreated() {return this == CREATED;}
    public boolean isCanceled() {return this == CANCELED;}
    public boolean isSuccess() {return this == SUCCESS;}


    public boolean canTransitionTo(OrderStatus nextStatus) {
        return switch (this) {
            case CREATED -> nextStatus == SUCCESS || nextStatus == CANCELED;
            case SUCCESS -> nextStatus == CANCELED; // 완료/취소는 더 이상 변경 불가
            case CANCELED -> false;
        };
    }
}
