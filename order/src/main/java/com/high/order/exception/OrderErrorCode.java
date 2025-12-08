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

    //Domain Exception
    ORDER_ITEM_NOT_FOUND(4001, HttpStatus.NOT_FOUND,"주문 아이템이 존재하지 않습니다."),
    INVALID_ORDER_STATUS(4002, HttpStatus.BAD_REQUEST, "주문 상태를 변경할 수 없습니다."),
    ORDER_CANCELLATION_NOT_ALLOWED(4003, HttpStatus.BAD_REQUEST, "주문을 취소할 수 없습니다."),
    ILLEGAL_ARGUMENT_EXCEPTION(4005, HttpStatus.BAD_REQUEST, "잘못된 요청값입니다."),

    //External Exception
    EMPTY_KAFKA_MESSAGE(4006, HttpStatus.NOT_FOUND,"kafka 메시지가 비어있습니다."),
    PRODUCT_NOT_FOUND(4007, HttpStatus.NOT_FOUND, "상품이 존재하지 않습니다.")

    ;


    private final int code;
    private final HttpStatus status;
    private final String message;


}
