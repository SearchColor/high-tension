package com.high.external.exception;

import com.library.module.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalErrorCode implements BaseErrorCode {


    /**
     * External - error 10000 번대
     */

    SlACK_RECORD_NOT_FOUND(10004, HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),

    SLACK_NOTIFICATION_FAILED(10500, HttpStatus.INTERNAL_SERVER_ERROR, "Slack DM 전송에 실패했습니다. (Slack 응답 오류)"),
    SLACK_API_CALL_ERROR(10500, HttpStatus.INTERNAL_SERVER_ERROR, "Slack API 호출 중 예측하지 못한 오류가 발생했습니다.")

    ;

    private final int code;
    private final HttpStatus status;
    private final String message;
}
