package com.high.order.domain.vo;

import com.high.order.domain.exception.DeliveryStatusChangeNotAllowedException;
import com.high.order.domain.exception.OrderCancellationNotAllowedByStatusException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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



    public void isUpdatableDeliveryInfo() {
        if(this == CANCELED) {
            log.error("주문이 취소되어 배송정보 변경이 불가능");
            throw new DeliveryStatusChangeNotAllowedException();
        }
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return switch (this) {
            case CREATED -> nextStatus == SUCCESS || nextStatus == CANCELED;
            case SUCCESS -> nextStatus == CANCELED; // 완료/취소는 더 이상 변경 불가
            case CANCELED -> false;
        };
    }

    public void validateCancellable() {
        if(! (this == CREATED || this == SUCCESS)) {
            log.info("현재 주문 상태 : {}", this);
            throw new OrderCancellationNotAllowedByStatusException();
        }
    }

    public void validatePartialCancellation() {
        if(! (this==CREATED)) {
            log.info("현재 주문 상태 : {}", this);
            throw new OrderCancellationNotAllowedByStatusException();
        }
    }
}
