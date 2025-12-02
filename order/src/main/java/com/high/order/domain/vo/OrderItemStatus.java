package com.high.order.domain.vo;

import lombok.Getter;

@Getter
public enum OrderItemStatus {
    CREATED("주문 생성"),
    CANCELED("주문 취소"),
    SUCCESS("주문 완료"),
    RETURN_REQUEST("반품 요청"),
    RETURNED("반품 완료");


    private final String description;

    OrderItemStatus(String description) {
        this.description = description;
    }

    public boolean isCreated() {return this == CREATED;}
    public boolean isCanceled() {return this == CANCELED;}
    public boolean isSuccess() {return this == SUCCESS;}
    public boolean isReturnRequest() {return this == RETURN_REQUEST;}
    public boolean isReturned() {return this == RETURNED;}

    public boolean canChangeStatus() {
        return this == SUCCESS || this == RETURN_REQUEST;
    }


    public boolean canTransitionTo(OrderItemStatus nextStatus) {
        return switch (this) {
            case CREATED -> nextStatus == SUCCESS || nextStatus == CANCELED;
            case SUCCESS -> nextStatus == RETURN_REQUEST || nextStatus == CANCELED;
            case RETURN_REQUEST -> nextStatus == RETURNED;
            case CANCELED, RETURNED -> false;
        };
    }
}
