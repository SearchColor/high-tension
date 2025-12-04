package com.high.orchestration.domain.repository;

import com.high.orchestration.domain.entity.SagaState;
import java.util.Optional;
import java.util.UUID;

public interface SagaStateRepository {
    SagaState save(SagaState sagaState);
    Optional<SagaState> findById(UUID sagaId);
}
