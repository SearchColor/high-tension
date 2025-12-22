package com.high.order.domain.vo;

import com.high.order.domain.exception.DeliveryStatusChangeNotAllowedException;
import com.high.order.domain.exception.OrderItemStatusChangeNotAllowedException;
import com.high.order.domain.exception.OrderPartialCancellationNotAllowedByItemStatusException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public void canChangeStatus() {
        if(! (this == SUCCESS || this == RETURN_REQUEST)) {
            log.info("아이템 상태 변경 불가 상태 (SUCCESS or RETURN_REQUEST 상태가 아님)");
            throw new OrderItemStatusChangeNotAllowedException();
        }
    }

    public void cannotChangeDeliveryStatus() {
        if(this == CREATED || this == CANCELED) {
            log.error("주문이 CREATED 상태이거나 CANCELED면 배송상태 변경 불가");
            throw new DeliveryStatusChangeNotAllowedException();
        }
    }


    public boolean canTransitionTo(OrderItemStatus nextStatus) {
        return switch (this) {
            case CREATED -> nextStatus == SUCCESS || nextStatus == CANCELED;
            case SUCCESS -> nextStatus == RETURN_REQUEST || nextStatus == CANCELED;
            case RETURN_REQUEST -> nextStatus == RETURNED;
            case CANCELED, RETURNED -> false;
        };
    }

    public void validateTransitionTo(OrderItemStatus nextStatus) {
        if (!canTransitionTo(nextStatus)) {
            log.error("잘못된 상태 전환 시도: {} -> {}", this, nextStatus);
            throw new OrderItemStatusChangeNotAllowedException();
        }
    }

    public void invalidPartialCancelRequest(OrderItemStatus nextStatus) {
        if (nextStatus.isCanceled()) {
            log.error("결제 완료 후에 부분취소 불가능");
            throw new OrderItemStatusChangeNotAllowedException();
        }
    }



    public void validatePartialCancellationForOrderItem() {
        if(! (this == CREATED)) {
            throw new OrderPartialCancellationNotAllowedByItemStatusException();
        }
    }
}
