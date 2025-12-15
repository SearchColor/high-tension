package com.high.order.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {

    //Application Exception
    BAD_REQUEST(4004, HttpStatus.BAD_REQUEST,"잘못된 요청입니다."),
    ORDER_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    NO_PERMISSION_TO_CHANGE_ORDER_ITEM_STATUS(4008, HttpStatus.FORBIDDEN,"주문 아이템 상태 변경 권한이 없습니다."),
    ORDER_ITEM_STATUS_CHANGE_NOT_ALLOWED(4009, HttpStatus.BAD_REQUEST, "주문 아이템 상태 변경이 불가능합니다."),
    NO_PERMISSION_TO_CANCEL_ORDER(4018, HttpStatus.BAD_REQUEST, "주문을 취소할 수 없습니다."),
    DELIVERY_STATUS_CHANGE_NOT_ALLOWED(4020, HttpStatus.BAD_REQUEST, "잘못된 배송상태 변경 요청입니다."),


    //Domain Exception
    ORDER_ITEM_NOT_FOUND(4001, HttpStatus.NOT_FOUND,"주문 아이템이 존재하지 않습니다."),
    INVALID_ORDER_STATUS(4002, HttpStatus.BAD_REQUEST, "주문 상태를 변경할 수 없습니다."),
    ORDER_CANCELLATION_NOT_ALLOWED(4003, HttpStatus.BAD_REQUEST, "주문을 취소할 수 없습니다."), //변경 필요
    ILLEGAL_ARGUMENT_EXCEPTION(4005, HttpStatus.BAD_REQUEST, "잘못된 요청값입니다."),

    //External Exception
    EMPTY_KAFKA_MESSAGE(4006, HttpStatus.NOT_FOUND,"kafka 메시지가 비어있습니다."),
    PRODUCT_NOT_FOUND(4007, HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다."),
    COUPON_NOT_FOUND(4012, HttpStatus.NOT_FOUND, "쿠폰이 존재하지 않습니다."),
    PAYMENT_NOT_FOUND(4015, HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),
    FEIGN_COMMUNICATION_ERROR(4019, HttpStatus.INTERNAL_SERVER_ERROR, "FeignClient 통신 오류가 발생하였습니다.")
    ;



    private final int code;
    private final HttpStatus status;
    private final String message;


}
