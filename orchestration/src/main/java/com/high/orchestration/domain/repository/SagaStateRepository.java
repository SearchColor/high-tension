package com.high.orchestration.domain.repository;

import com.high.orchestration.domain.entity.SagaState;

public interface SagaStateRepository {
    SagaState save(SagaState sagaState);
}
