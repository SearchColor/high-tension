package com.high.orchestration.domain.vo;

import lombok.Getter;

@Getter
public enum CurrentStep {

    ORDER_CREATE_VALIDATE("주문 생성 - 주문 처리",1,SagaType.ORDER_CREATE),
    ORDER_CREATE_STOCK("주문 생성 - 재고 차감",2,SagaType.ORDER_CREATE),
    ORDER_CREATE_PAYMENT("주문 생성 - 결제 처리",3,SagaType.ORDER_CREATE),
    ORDER_CREATE_COMPLETE("주문 생성 - 완료",4,SagaType.ORDER_CREATE),

    ORDER_CANCEL_VALIDATE("주문 취소 - 주문 삭제",1,SagaType.ORDER_CANCEL),
    ORDER_CANCEL_STOCK_RESTORE("주문 취소- 재고 복원",2,SagaType.ORDER_CANCEL),
    ORDER_CANCEL_REFUND("주문 취소 - 결제 취소",3,SagaType.ORDER_CANCEL),
    ORDER_CANCEL_COMPLETE("주문 취소 - 완료",4,SagaType.ORDER_CANCEL),

    REFUND_VALIDATE("환불 - 주문 환불 처리",1,SagaType.ORDER_REFUND),
    REFUND_EXECUTE("환불 - 결제 환불 처리",2,SagaType.ORDER_REFUND),
    REFUND_COMPLETE("환불 - 완료",3,SagaType.ORDER_REFUND),;



    private final String description;
    private final int seq;
    private final SagaType sagaType;


    CurrentStep(String description, int seq, SagaType sagaType) {
        this.description = description;
        this.seq = seq;
        this.sagaType = sagaType;
    }

    public boolean isAfter(CurrentStep step) {
        if(!this.sagaType.equals(step.sagaType)) {
            return false;
        }
        return this.seq > step.seq;
    }
}
