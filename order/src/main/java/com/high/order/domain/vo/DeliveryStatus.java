package com.high.order.domain.vo;

import com.high.order.domain.exception.DeliveryStatusChangeNotAllowedException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum DeliveryStatus {

    READY("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public boolean isUpdatableDeliveryInfo() {
        return this == READY;
    }

    public boolean canTransitionToRefund() {
        return this == DELIVERED;
    }

    public boolean canTransitionTo(DeliveryStatus nextStatus) {
        return switch (this) {
            case READY -> nextStatus == SHIPPING;
            case SHIPPING -> nextStatus == DELIVERED;
            case DELIVERED -> false;
        };
    }

    public void validateTransition(DeliveryStatus nextStatus) {
        if(!canTransitionTo(nextStatus)) {
            log.error("잘못된 상태 전환 시도: {} -> {}", this, nextStatus);
            throw new DeliveryStatusChangeNotAllowedException();
        }
    }
}
