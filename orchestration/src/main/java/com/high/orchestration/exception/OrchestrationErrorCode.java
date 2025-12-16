package com.high.orchestration.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrchestrationErrorCode implements BaseErrorCode {

    //응용 계층 예외
    FAILED_TO_START_SAGA(8000, HttpStatus.BAD_REQUEST, "Saga를 시작에 실패하였습니다."),
    SAGA_STATE_NOT_FOUND(8001, HttpStatus.NOT_FOUND, "Saga State를 찾을 수 없습니다."),
    FAILED_TO_INITIALIZATION(8002, HttpStatus.BAD_REQUEST, "Saga 테이블 생성에 실패하였습니다."),

    //도메인 계층 예외

    //인프라
    FAILED_TO_CONVERT_MESSAGE(8003, HttpStatus.BAD_REQUEST, "메시지를 변환할 수 없습니다."),
    MESSAGE_PUBLISH_INTERRUPTED_EXCEPTION(8004, HttpStatus.INTERNAL_SERVER_ERROR, "메시지 발행 중 스레드 인터럽트가 발생하였습니다."),
    MESSAGE_PUBLISH_FAILED_EXCEPTION(8005, HttpStatus.SERVICE_UNAVAILABLE, "메시지 발행에 실패ㅑ하였습니다."),
    MESSAGE_SERIALIZATION_EXCEPTION(8006, HttpStatus.BAD_REQUEST, "Kafka 메시지 직렬화에 실패하였습니다." );

    private final int code;
    private final HttpStatus status;
    private final String message;
}
