package com.high.order.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {

    //Application Exception
    ORDER_NOT_FOUND(4101, HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    ORDER_ITEM_NOT_FOUND(4102, HttpStatus.NOT_FOUND,"주문 아이템이 존재하지 않습니다."), //application 예외
    NO_PERMISSION_TO_CHANGE_ORDER_ITEM_STATUS(4103, HttpStatus.UNAUTHORIZED,"주문 아이템 상태 변경 권한이 없습니다."),
    ILLEGAL_CONTACT_FORMAT_REQUEST(4105, HttpStatus.BAD_REQUEST, "잘못된 연락처 요청 형식입니다."),

    //Application (feign client info exception)
    PAYMENT_NOT_CANCELABLE(4106, HttpStatus.BAD_REQUEST,"현재 결제 상태에서는 주문을 취소할 수 없습니다." ),
    PAYMENT_NOT_FOUND(4107, HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."), //feign
    PAYMENT_NOT_REFUNDABLE(4108, HttpStatus.BAD_REQUEST, "현재 결제 상태에서는 환불 신청할 수 없습니다."),

    //Domain Exception
    ORDER_STATUS_CHANGE_NOT_ALLOWED(4001, HttpStatus.BAD_REQUEST, "주문 상태를 변경할 수 없습니다."),
    ORDER_ITEM_STATUS_CHANGE_NOT_ALLOWED(4002, HttpStatus.BAD_REQUEST, "주문 아이템 상태 변경이 불가능합니다."),
    ORDER_ITEM_NOT_FOUND_IN_ORDER(4003, HttpStatus.NOT_FOUND, "주문에 해당 상품이 존재하지 않습니다."),
    ORDER_CANCELLATION_NOT_ALLOWED_BY_STATUS(4005, HttpStatus.BAD_REQUEST, "주문을 취소할 수 없는 주문 상태입니다."),
    ORDER_CANCELLATION_NOT_ALLOWED_BY_ITEM_STATUS(4006, HttpStatus.BAD_REQUEST, "취소할 수 없는 주문 상품이 존재하여 전체 주문을 취소할 수 없습니다." ),
    ORDER_PARTIAL_CANCELLATION_NOT_ALLOWED_BY_ITEM_STATUS(4007, HttpStatus.BAD_REQUEST, "주문을 취소할 수 없는 주문 상품 상태입니다."),
    DELIVERY_STATUS_CHANGE_NOT_ALLOWED(4008, HttpStatus.BAD_REQUEST, "잘못된 배송상태 변경 요청입니다."), //도메인 예외

    //External Exception
    EMPTY_KAFKA_MESSAGE(4201, HttpStatus.BAD_REQUEST,"kafka 메시지가 비어있습니다."),
    FEIGN_COMMUNICATION_ERROR(4202, HttpStatus.INTERNAL_SERVER_ERROR, "FeignClient 통신 오류가 발생하였습니다."),
    ;



    private final int code;
    private final HttpStatus status;
    private final String message;


}
