package com.high.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.domain.model.ProcessedSaga;
import com.high.product.domain.repository.ProcessedSagaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SagaProcessingService {

	private final ProcessedSagaRepository processedSagaRepository;

	/**
	 * 처음 처리하는 경우 true, 이미 처리된 saga면 false 반환
	 * 동시성 경쟁은 DB PK 제약으로도 방어(예외 handling 필요)
	 */
	public boolean markIfNotProcessed(UUID sagaId, UUID orderId, String sagaType) {
		if (processedSagaRepository.existsById(sagaId))
			return false;
		processedSagaRepository.save(new ProcessedSaga(sagaId, orderId, sagaType, "PROCESSED"));
		return true;
	}

	public void markFailed(UUID sagaId, UUID orderId, String sagaType) {
		if (processedSagaRepository.existsById(sagaId))
			return;
		processedSagaRepository.save(new ProcessedSaga(sagaId, orderId, sagaType, "FAILED"));
	}
}