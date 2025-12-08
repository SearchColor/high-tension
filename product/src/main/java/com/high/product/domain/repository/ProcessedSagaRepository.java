package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.high.product.domain.model.ProcessedSaga;

public interface ProcessedSagaRepository {

	Optional<ProcessedSaga> findById(UUID sagaId);

	ProcessedSaga save(ProcessedSaga saga);

	boolean existsById(UUID sagaId);
}
