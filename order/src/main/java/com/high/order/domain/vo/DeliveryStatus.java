package com.high.order.domain.vo;

import lombok.Getter;

@Getter
public enum DeliveryStatus {

    READY("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }
}
