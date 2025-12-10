package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.high.product.domain.model.ProcessedSaga;
import com.high.product.domain.repository.ProcessedSagaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProcessedSagaRepositoryAdapter implements ProcessedSagaRepository {

	private final JpaProcessedSagaRepository jpaRepository;

	@Override
	public Optional<ProcessedSaga> findById(UUID sagaId) {
		return jpaRepository.findById(sagaId);
	}

	@Override
	public ProcessedSaga save(ProcessedSaga saga) {
		return jpaRepository.save(saga);
	}

	@Override
	public boolean existsById(UUID sagaId) {
		return jpaRepository.existsById(sagaId);
	}
}