package com.high.orchestration.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrchestrationErrorCode implements BaseErrorCode {

    //응용 계층 예외
    FAILED_TO_START_SAGA(8000, HttpStatus.BAD_REQUEST, "Saga를 시작에 실패하였습니다.");

    //도메인 계층 예외

    private final int code;
    private final HttpStatus status;
    private final String message;
}
