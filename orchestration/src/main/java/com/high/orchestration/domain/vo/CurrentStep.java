package com.high.orchestration.domain.vo;

import lombok.Getter;

@Getter
public enum CurrentStep {

    ORDER_CREATE_VALIDATE("주문 생성 - 주문 처리"),
    ORDER_CREATE_STOCK("주문 생성 - 재고 차감"),
    ORDER_CREATE_PAYMENT("주문 생성 - 결제 처리"),
    ORDER_CREATE_COMPLETE("주문 생성 - 완료"),

    ORDER_CANCEL_VALIDATE("주문 취소 - 주문 삭제"),
    ORDER_CANCEL_STOCK_RESTORE("주문 취소- 재고 복원"),
    ORDER_CANCEL_REFUND("주문 취소 - 결제 취소"),
    ORDER_CANCEL_COMPLETE("주문 취소 - 완료"),

    REFUND_VALIDATE("환불 - 주문 환불 처리"),
    REFUND_EXECUTE("환불 - 결제 환불 처리"),
    REFUND_COMPLETE("환불 - 완료");



    private final String description;

    CurrentStep(String description) {
        this.description = description;
    }
}
