package com.high.orchestration.monitoring.domain;

import lombok.Getter;

@Getter
public enum DlqStatus {

    RETRY_WAITING("재시도 대기중"),
    RETRYING("재시도 진행 중"),
    RETRY_SUCCESS("재시도 성공"),
    PERMANENT_FAIL("처리 실패");
    private final String description;

    DlqStatus(String description) {this.description = description;}
}
