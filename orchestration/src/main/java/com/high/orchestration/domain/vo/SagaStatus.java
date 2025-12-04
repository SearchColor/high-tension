package com.high.orchestration.domain.vo;

import lombok.Getter;

@Getter
public enum SagaStatus {

    STARTED("SAGA 프로세스 시작"),
    COMPLETED("SAGA 프로세스 완료"),
    FAILED("SAGA 프로세스 처리 중 실패"),
    COMPENSATING("보상 트랜잭션 실행 중"),
    COMPENSATED("보상 트랜잭선 완료");

    private final String description;

    SagaStatus(String description) {
        this.description = description;
    }
}
